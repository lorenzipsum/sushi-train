# 2026-03-01 - Conversation Documentation Setup

## Context

We discussed how to document project conversations in a durable and searchable way in the repository.

## Questions Asked

- Is there a way to document all project conversations?
- How could that look?
- Can this be done for project chat history going backwards?

## Key Guidance

- Store conversation summaries in `docs/conversations/` as project artifacts.
- Use one markdown file per session named `YYYY-MM-DD-topic.md`.
- Maintain an index file for discoverability.
- Record durable architecture/process decisions as ADRs in `docs/adr/`.

## Decisions Made

- Decision: Introduce a repository-based conversation logging structure and ADR support.
- Rationale: Keeps chat context searchable, reviewable, and tied to project history.
- Owner: Project team.
- Date: 2026-03-01.

## Action Items

- [ ] Backfill older sessions from exported chat history into dated files.
- [ ] Add conversation logging step to team workflow/definition of done.

## References

- Files: `docs/conversations/README.md`, `docs/conversations/_template.md`, `docs/adr/README.md`.
