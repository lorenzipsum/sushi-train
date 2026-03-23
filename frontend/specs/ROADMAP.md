# Roadmap

This document gives a high-level overview of the shipped frontend features and what each one adds to the sushi-train experience.

## Feature Overview

| Feature | Overview |
| ------- | -------- |
| `001-belt-visualization` | Introduces the baseline sushi belt view with slot rendering, seat placement, and the initial read-only stage presentation. |
| `002-belt-layout-redesign` | Reworks the belt-stage layout into the current racetrack counter presentation with richer stage composition and improved spatial readability. |
| `003-occupy-seat` | Adds the ability to select a seat and start dining, including the first seat-lifecycle state transitions and selected-seat context. |
| `004-checkout-seat` | Adds checkout behavior for occupied seats, including end-of-meal flow and final order summary handling. |
| `005-pick-plates` | Adds guest plate picking from the moving belt, including pickability rules, order-line creation, and pick feedback states. |
| `006-hydrate-seat-orders` | Restores occupied-seat and order context from backend data so reloaded sessions preserve dining continuity and selected-seat detail. |
| `007-add-plates-belt` | Adds the operator-facing plate placement flow for demo mode, including menu search, plate draft entry, and add-to-belt submission from the main UI. |
| `008-design-polish` | Applies the current visual direction, readability tuning, and overall presentation polish across the main belt experience. |
| `009-refine-belt-interactions` | Refines seat interaction clarity with improved reach visuals, stronger selected-seat emphasis, ring-only moving plates, and belt speed control via modal. |
| `realtime-belt-updates` | Adds realtime belt updates with server-sent events and polling fallback so belt motion and stage state stay synchronized with backend changes. |
