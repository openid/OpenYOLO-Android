# Trapdoor - a minimal credential provider

Trapdoor is much simpler credential provider than Barbican, with no storage and a very simple
cryptographic operation to generate passwords for apps. It transforms a 6-digit pin and an
app identifier into a 16 character password. This approach is fine for a demo, but is riddled
with issues for a real world password manager - DO NOT copy this approach for any real world
usage.
