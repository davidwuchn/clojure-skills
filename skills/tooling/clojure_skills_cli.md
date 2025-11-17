---
name: clojure-skills-cli
description: |
  Manage and search Clojure skills and prompts with the clojure-skills CLI tool.
  Use when managing skills library, searching for relevant skills, querying the
  skills database, or when the user mentions skill management, prompt discovery,
  database search, or skills inventory.
---

# clojure-skills CLI

Command-line tool for managing and searching the Clojure skills library with a SQLite backend.

## Quick Start

```bash
# Initialize database (first time only)
clojure-skills init

# Sync skills and prompts
clojure-skills sync

# Search for skills
clojure-skills search "http server"

# List all skills
clojure-skills list-skills

# View statistics
clojure-skills stats
```

**Database location:** `~/.config/clojure-skills/clojure-skills.db`

## Commands

### init
Create the database. Run once per system.

### sync
Index all skills/prompts from `skills/` and `prompts/` directories. Run after adding or modifying files.

### search
```bash
clojure-skills search "query" [options]

OPTIONS:
  -c, --category S          Filter by category
  -t, --type S         all  Search type (skills, prompts, or all)
  -n, --max-results N  50   Maximum results
```

Full-text search across skill names, content, and categories.

### list-skills
```bash
clojure-skills list-skills [--category S]
```

List all skills in tabular format with size and token counts.

### list-prompts
List all composed prompts with metadata.

### show-skill
```bash
clojure-skills show-skill <name> [--category S]
```

Export skill content as JSON for programmatic use.

### stats
Show database statistics including total skills, prompts, categories, size, and tokens.

### reset-db
Delete all data and reinitialize (requires `--force` flag).

## Categories

Skills organized by directory structure:
- `language/` - Core Clojure concepts
- `libraries/<subcategory>/` - Third-party libraries
- `testing/` - Test frameworks
- `tooling/` - Development tools
- `http_servers/` - Web servers
- `clojure_mcp/` - MCP integration

## Common Workflows

**Initial setup:**
```bash
clojure-skills init
clojure-skills sync
```

**Search by topic:**
```bash
clojure-skills search "database"
clojure-skills search "http" --max-results 10
clojure-skills search "validation" --type skills
```

**Browse by category:**
```bash
clojure-skills list-skills --category "libraries/database"
clojure-skills list-skills --category "testing"
```

**After adding/modifying skills:**
```bash
clojure-skills sync
```

## Common Issues

**Database not found:**
```bash
clojure-skills init
clojure-skills sync
```

**Search returns no results after adding skill:**
```bash
clojure-skills sync  # Re-index
```

**Category filter not working:**
Use full path: `--category "libraries/database"` not `--category "database"`

**Too many results:**
```bash
clojure-skills search "test" --category testing --max-results 10
```

## Key Features

- **Full-text search** - Fast queries across all content
- **Change detection** - SHA-256 hashing for incremental sync
- **Token estimation** - Approximate tokens (chars / 4)
- **JSON export** - `show-skill` for programmatic access
- **Categories** - Hierarchical organization

## Integration

**Use in shell scripts:**
```bash
#!/bin/bash
skills=$(clojure-skills search "database" --type skills)
skill_data=$(clojure-skills show-skill "next_jdbc")
echo "$skill_data" | jq '.content'
```

**Babashka tasks:**
```clojure
{:tasks
 {search-skills
  {:doc "Search skills"
   :task (shell "clojure-skills" "search" (first *command-line-args*))}}}
```

## Summary

Essential workflow:
```bash
clojure-skills init              # Once per system
clojure-skills sync              # After changes
clojure-skills search "topic"    # Find skills
clojure-skills list-skills       # Browse all
clojure-skills stats             # Overview
```

Use the CLI to discover, search, and manage the skills library efficiently.
