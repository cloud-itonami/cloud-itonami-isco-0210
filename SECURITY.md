# Security Policy

## Reporting a Vulnerability

If you discover a security vulnerability in cloud-itonami-isco-0210, please report it by emailing security@cloud-itonami.org. Do not open a public issue for security vulnerabilities.

Please include:

- A description of the vulnerability
- Steps to reproduce (if applicable)
- Potential impact
- Your name and contact information

## Supported Versions

Security updates are provided for the current release and the previous major version.

## Security Considerations

This actor is designed with multiple safety layers:

1. **Closed allowlist**: Only explicitly permitted operations can be proposed
2. **Hard invariants**: Certain operations are permanently forbidden and cannot be overridden
3. **Governor-based gatekeeping**: All proposals pass through an independent governor before execution
4. **Audit trail**: All administrative actions are recorded in an append-only ledger
5. **Human-in-the-loop**: Certain operations require explicit human approval before proceeding

The actor is not designed for real-time control or high-frequency autonomous decision making. It is specifically for administrative and logistical support with human oversight built into its governance model.

## Scope Boundaries

This actor explicitly excludes:

- Personnel deployment or operational command decisions
- Weapons systems or combat operations
- Access to classified or operational data
- Lethal autonomous decision making
- Personnel policy or disciplinary authority

Any proposed changes or configurations that attempt to extend the actor into these areas should be reported as security concerns.
