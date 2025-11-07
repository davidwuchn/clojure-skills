# Clojure Skills

A collection of System Prompt fragments that can make working with
Clojure in [opencode](https://opencode.ai/) more effective.

This project consists of a collection of Skills and prompts that you
can mix and compose to create an effective coding agent for your code
base.

> **For LLM Agents:** See [AGENT.md](AGENT.md) for comprehensive
> guidance on working with this repository.


Skills follow Anthropic Skills API and should be compatible.

This tool also allows you to combine these skills into directed
prompts that you can using in coding agents like opencode.

The goal is to allow us to capture to best work with Clojure from
within LLM agents in a vendor neutral format.

## Skills

These skills are broken into different subsections,

- clojure-mcp
- http-server
- libraries
- language
- testing
- tooling

## Prompts

Skills can be combined into prompts that OpenCode is able to
use. See prompts/clojure_build.md for an example.

## Development

### Installation

#### MacOS

On macos you can install the dependencies for this project with brew.

```shell
brew bundle
```

### DNF/RPM system

```shell
sudo dnf install just pandoc jq
```

### Spell Checking

This project uses [typos](https://github.com/crate-ci/typos) for spell
checking source code.

**Check for typos:**
```shell
make typos
# or
bb typos
```

**Automatically fix typos:**
```shell
make typos-fix
# or
bb typos-fix
```

**Configuration:**

The `_typos.toml` file contains project-specific configuration for
handling false positives and excluding directories. See the [typos
documentation](https://github.com/crate-ci/typos/blob/master/docs/reference.md)
for more details.

### For Contributors

See [AGENT.md](AGENT.md) for:
- Complete repository structure
- Development workflow guidelines
- Common tasks and commands
- Best practices for skills and prompts
- Troubleshooting guide
