# Contributing

We welcome contributions to cloud-itonami-isco-0210. This project is open source under the AGPL-3.0-or-later license.

## Getting Started

1. Fork the repository
2. Clone your fork locally
3. Create a branch for your changes
4. Make your changes and add tests
5. Run the test suite to ensure everything passes
6. Submit a pull request

## Development

To run tests locally:

```bash
kbb -M:test
```

## Scope Boundaries

Remember that this actor is for administrative support only. Any proposed changes that touch the following areas are out of scope and will be rejected:

- Personnel deployment or operational command
- Weapons systems or combat operations
- Classified or operational data
- Lethal autonomous decisions
- Personnel policy or disciplinary authority

## Testing

All changes should include tests. We use `cognitect-labs/test-runner` for running tests.

## Code Style

- Follow Clojure conventions
- Use meaningful variable and function names
- Include docstrings for public functions
- Keep functions focused and testable

## Reporting Issues

Please report issues through GitHub issues. Include:

- A clear description of the problem
- Steps to reproduce (if applicable)
- Expected vs. actual behavior
- Your environment (OS, Clojure version, etc.)

## License

By contributing, you agree that your contributions will be licensed under the AGPL-3.0-or-later license.
