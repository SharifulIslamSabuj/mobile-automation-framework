# Test Resources — Config

Environment/platform configuration consumed at test runtime by `ConfigReader`:

- `config.properties` — common values shared by every environment
- `config-emulator.properties` — overrides active when `-Denv=emulator` (the default)
- `config-real-device.properties` — overrides active when `-Denv=real-device`

See [docs/framework/CONFIGURATION_ARCHITECTURE.md](../../../../docs/framework/CONFIGURATION_ARCHITECTURE.md) for the full loading order and hierarchy.
