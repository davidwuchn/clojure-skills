.DEFAULT_GOAL := _build/clojure_build.md

.PHONY: typos typos-fix clean help

# Helper: Extract metadata from prompt files using Pandoc + jq
# Usage: pandoc <file> --template=prompt_templates/metadata.plain | jq '.sections[]?'
# This uses Pandoc's $meta-json$ template variable to extract YAML frontmatter

_build/%.md: prompts/%.md
	pandoc prompts/$*.md -o $@
	@echo "build successful: $@"

# Check for typos in the codebase
typos:
	@echo "Checking for typos..."
	typos

# Fix typos automatically
typos-fix:
	@echo "Fixing typos..."
	typos --write-changes

# Show available targets
help:
	@echo "Available targets:"
	@echo "  make                  - Build _build/clojure_build.md (default)"
	@echo "  make typos            - Check for typos"
	@echo "  make typos-fix        - Fix typos automatically"
	@echo "  make _build/%.md      - Build specific markdown file"
	@echo "  make help             - Show this help message"

clean:
	rm -rf _build/*.md
