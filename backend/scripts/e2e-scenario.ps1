param(
    [string]$BaseUrl = "http://localhost:8088",
    [int]$CreateBatches = 5,
    [int]$PlatesPerBatch = 10,
    [int]$PickCount = 5,
    [int]$ExpireOnBeltCount = 3,
    [Nullable[int]]$Seed = $null
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Assert-True {
    param(
        [bool]$Condition,
        [string]$Message
    )
    if (-not $Condition) {
        throw "Assertion failed: $Message"
    }
}

function Parse-JsonOrNull {
    param([string]$Text)
    if ([string]::IsNullOrWhiteSpace($Text)) {
        return $null
    }
    try {
        return $Text | ConvertFrom-Json
    } catch {
        return $Text
    }
}

function Invoke-Api {
    param(
        [ValidateSet("GET", "POST", "PATCH")]
        [string]$Method,
        [string]$Path,
        [object]$Body = $null,
        [int[]]$ExpectedStatus = @(200)
    )

    $uri = "$BaseUrl$Path"
    $headers = @{ "Accept" = "application/json" }
    $bodyJson = $null
    if ($null -ne $Body) {
        $bodyJson = $Body | ConvertTo-Json -Depth 10
        $headers["Content-Type"] = "application/json"
    }

    try {
        $response = Invoke-WebRequest -Method $Method -Uri $uri -Headers $headers -Body $bodyJson -UseBasicParsing
        $status = [int]$response.StatusCode
        $parsed = Parse-JsonOrNull -Text $response.Content
    } catch {
        if ($null -eq $_.Exception.Response) {
            throw
        }
        $status = [int]$_.Exception.Response.StatusCode
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $raw = $reader.ReadToEnd()
        $parsed = Parse-JsonOrNull -Text $raw
    }

    if ($ExpectedStatus -notcontains $status) {
        throw "Unexpected status $status for $Method $Path (expected: $($ExpectedStatus -join ', '))."
    }

    return [pscustomobject]@{
        StatusCode = $status
        Body       = $parsed
    }
}

function Get-PagedItems {
    param([object]$PagedBody)
    if ($null -eq $PagedBody) { return ,@() }
    if ($PagedBody.PSObject.Properties.Name -contains "content") {
        return ,@($PagedBody.content)
    }
    if ($PagedBody.PSObject.Properties.Name -contains "items") {
        return ,@($PagedBody.items)
    }
    return ,@()
}

function Pick-RandomSubset {
    param(
        [object[]]$Items,
        [int]$Count
    )
    if ($Count -le 0 -or $Items.Count -eq 0) {
        return ,@()
    }
    $effective = [Math]::Min($Count, $Items.Count)
    return ,@($Items | Get-Random -Count $effective)
}

if ($null -ne $Seed) {
    Write-Host "Using deterministic seed: $Seed" -ForegroundColor DarkGray
    Get-Random -SetSeed $Seed | Out-Null
}

Write-Host "Base URL: $BaseUrl" -ForegroundColor DarkGray

# 1) First menu item
Write-Step "Fetch first menu item"
$menuResp = Invoke-Api -Method GET -Path "/api/v1/menu-items?page=0&size=1" -ExpectedStatus @(200)
$menuItems = @(Get-PagedItems -PagedBody $menuResp.Body)
Assert-True ($menuItems.Count -ge 1) "No menu items returned."
$menuItem = $menuItems[0]
$menuItemId = [string]$menuItem.id
Write-Host "Menu item: $($menuItem.name) ($menuItemId)"

# 2) Only existing belt
Write-Step "Fetch belts"
$beltsResp = Invoke-Api -Method GET -Path "/api/v1/belts" -ExpectedStatus @(200)
$belts = @($beltsResp.Body)
Assert-True ($belts.Count -ge 1) "No belts returned."
if ($belts.Count -ne 1) {
    Write-Host "Warning: expected 1 belt, got $($belts.Count). Using first belt." -ForegroundColor Yellow
}
$beltId = [string]$belts[0].id
Write-Host "Using belt: $beltId"

# 3) Create 5 * 10 plates on belt
Write-Step "Create $CreateBatches batches with $PlatesPerBatch plates each"
$createdPlateIds = New-Object System.Collections.Generic.List[string]
for ($i = 1; $i -le $CreateBatches; $i++) {
    $createBody = @{
        menuItemId   = $menuItemId
        numOfPlates  = $PlatesPerBatch
    }
    $createResp = Invoke-Api -Method POST -Path "/api/v1/belts/$beltId/plates" -Body $createBody -ExpectedStatus @(201)
    Assert-True ([int]$createResp.Body.createdCount -eq $PlatesPerBatch) "Batch $i did not create expected plate count."
    foreach ($p in @($createResp.Body.placedPlates)) {
        $createdPlateIds.Add([string]$p.plateId)
    }
    Write-Host "Batch $i created: $($createResp.Body.createdCount)"
}
Assert-True ($createdPlateIds.Count -eq ($CreateBatches * $PlatesPerBatch)) "Unexpected total created plate count."

# 4) Occupy first seat
Write-Step "Fetch first two seats from belt"
$fullBeltResp = Invoke-Api -Method GET -Path "/api/v1/belts/$beltId" -ExpectedStatus @(200)
$seats = @($fullBeltResp.Body.seats | Sort-Object positionIndex)
Assert-True ($seats.Count -ge 2) "Need at least 2 seats on belt."
$seat1Id = [string]$seats[0].id
$seat2Id = [string]$seats[1].id
Write-Host "Seat #1: $seat1Id, Seat #2: $seat2Id"

Write-Step "Occupy first seat"
Invoke-Api -Method POST -Path "/api/v1/seats/$seat1Id/occupy" -ExpectedStatus @(201) | Out-Null

# 5) Pick 5 random plates
Write-Step "Pick $PickCount random plates on first seat"
$pickedPlateIds = New-Object System.Collections.Generic.List[string]
$pickCandidates = Pick-RandomSubset -Items @($createdPlateIds) -Count $PickCount
Assert-True ($pickCandidates.Count -eq [Math]::Min($PickCount, $createdPlateIds.Count)) "Not enough pick candidates."
foreach ($plateId in $pickCandidates) {
    Invoke-Api -Method POST -Path "/api/v1/seats/$seat1Id/order-lines" -Body @{ plateId = $plateId } -ExpectedStatus @(201) | Out-Null
    $pickedPlateIds.Add($plateId)
}
Write-Host "Picked plates: $($pickedPlateIds.Count)"

# 6) Checkout first seat
Write-Step "Checkout first seat"
Invoke-Api -Method POST -Path "/api/v1/seats/$seat1Id/checkout" -ExpectedStatus @(200) | Out-Null

# 7) Occupy second seat
Write-Step "Occupy second seat"
Invoke-Api -Method POST -Path "/api/v1/seats/$seat2Id/occupy" -ExpectedStatus @(201) | Out-Null

# 8) Checkout second seat without picks
Write-Step "Checkout second seat without picked plates"
Invoke-Api -Method POST -Path "/api/v1/seats/$seat2Id/checkout" -ExpectedStatus @(200) | Out-Null

# 9) Expire 3 random on-belt plates (from created minus picked)
Write-Step "Expire $ExpireOnBeltCount random on-belt plates"
$pickedSet = New-Object 'System.Collections.Generic.HashSet[string]'
foreach ($p in $pickedPlateIds) { [void]$pickedSet.Add($p) }
$onBeltCandidates = @()
foreach ($p in $createdPlateIds) {
    if (-not $pickedSet.Contains($p)) {
        $onBeltCandidates += $p
    }
}
$expireTargets = Pick-RandomSubset -Items $onBeltCandidates -Count $ExpireOnBeltCount
foreach ($plateId in $expireTargets) {
    Invoke-Api -Method POST -Path "/api/v1/plates/$plateId/expire" -ExpectedStatus @(200) | Out-Null
}
Write-Host "Expired on-belt plates: $($expireTargets.Count)"

# 10) Expire picked plate (expected failure)
Write-Step "Try to expire one picked plate (expected failure)"
Assert-True ($pickedPlateIds.Count -ge 1) "No picked plate available for negative test."
$pickedPlateToExpire = $pickedPlateIds[0]
$expirePickedResp = Invoke-Api -Method POST -Path "/api/v1/plates/$pickedPlateToExpire/expire" -ExpectedStatus @(400, 409, 500)
Write-Host "Expire picked plate returned HTTP $($expirePickedResp.StatusCode) (expected non-success)"

# 11) Change belt speed
Write-Step "Change belt speed"
$beltBeforeResp = Invoke-Api -Method GET -Path "/api/v1/belts/$beltId" -ExpectedStatus @(200)
$currentSpeed = [int]$beltBeforeResp.Body.speedSlotsPerTick
$newSpeed = if ($currentSpeed -eq 1) { 2 } else { 1 }
$patchResp = Invoke-Api -Method PATCH -Path "/api/v1/belts/$beltId" -Body @{ speedSlotsPerTick = $newSpeed } -ExpectedStatus @(200)
Assert-True ([int]$patchResp.Body.speedSlotsPerTick -eq $newSpeed) "Belt speed was not updated."
Write-Host "Belt speed changed: $currentSpeed -> $newSpeed"

Write-Host ""
Write-Host "Scenario completed successfully." -ForegroundColor Green
Write-Host "Summary:"
Write-Host "  Created plates : $($createdPlateIds.Count)"
Write-Host "  Picked plates  : $($pickedPlateIds.Count)"
Write-Host "  Expired on-belt: $($expireTargets.Count)"
Write-Host "  Expire picked  : HTTP $($expirePickedResp.StatusCode)"
