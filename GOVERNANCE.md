# Governance

This project is part of the cloud-itonami open-source initiative for transparent, auditable ISCO-08 occupation blueprints.

## Decision Making

- Major design decisions are made through open issues and pull requests
- All significant changes require at least one review from a core contributor
- Security and governance invariants are non-negotiable and documented in the README

## Safety Invariants

The actor's governor enforces permanently forbidden operations:
- Personnel deployment, assignment, or operational command
- Weapons systems, munitions, targeting, or combat operations
- Operational, classified, or real-time command data
- Lethal autonomous decisions
- Personnel policy decisions or disciplinary authority

These boundaries are part of the design contract and cannot be overridden by governance configuration.

## Contributing

See CONTRIBUTING.md for guidelines on reporting issues, submitting pull requests, and development practices.

## Security

See SECURITY.md for information about reporting security vulnerabilities.
