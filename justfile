# Default recipe - build all prompts
default: build-all

# Build all prompt files
build-all:
    @just build clojure_skill_builder
    @just build clojure_build

# Build a specific prompt file by name (without .md extension)
# Usage: just build clojure_skill_builder
build name:
    #!/bin/bash
    set -e
    INPUT="prompts/{{name}}.md"
    OUTPUT="_build/{{name}}.md"
    TEMPLATE="prompt_templates/metadata.plain"

    if [ ! -f "$INPUT" ]; then
        echo "Error: Input file not found: $INPUT"
        exit 1
    fi

    echo "Building $OUTPUT..."
    mkdir -p _build

    # Copy main file preserving frontmatter
    cp "$INPUT" "$OUTPUT"

    # Extract sections from metadata and append each
    sections=$(pandoc "$INPUT" --template="$TEMPLATE" | jq -r '.sections[]? // empty')

    for section in $sections; do
        if [ -f "$section" ]; then
            echo "" >> "$OUTPUT"
            echo "# Including: $section" >> "$OUTPUT"
            echo "" >> "$OUTPUT"
            # Use pandoc to strip frontmatter and output markdown
            pandoc "$section" -f markdown -t markdown >> "$OUTPUT"
        else
            echo "Warning: Section file not found: $section"
        fi
    done

    echo "Build successful: $OUTPUT"

# Check for typos
typos:
    @echo "Checking for typos..."
    @typos

# Fix typos automatically
typos-fix:
    @echo "Fixing typos..."
    @typos --write-changes

# Clean build artifacts
clean:
    @echo "Cleaning _build..."
    @rm -rf _build/*.md

# Show available recipes
help:
    @just --list
