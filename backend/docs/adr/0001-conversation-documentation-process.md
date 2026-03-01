# ADR 0001 - Conversation Documentation Process

## Status

Accepted

## Context

Project decisions and rationale are often discussed in chat, which is hard to search and easy to lose over time.

## Decision

Adopt a repository-based documentation process:

- Summarize each project conversation in `docs/conversations/YYYY-MM-DD-topic.md`.
- Maintain `docs/conversations/README.md` as the index of sessions.
- Record durable decisions in ADRs under `docs/adr/`.
- Keep summaries concise and avoid committing sensitive raw transcripts.

## Consequences

- Positive: Better traceability for decisions and action items.
- Positive: Easier onboarding because rationale is in the repository.
- Negative: Requires discipline to keep logs updated.
- Follow-up: Backfill historical sessions from exported logs when available.

## Related Conversations

- `docs/conversations/2026-03-01-conversation-documentation.md`
