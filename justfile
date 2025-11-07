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
    
    PROMPT="prompts/{{name}}.md"
    METADATA="prompt_templates/{{name}}.yaml"
    OUTPUT="_build/{{name}}.md"
    TEMPLATE="prompt_templates/combine.md"
    
    if [ ! -f "$PROMPT" ]; then
        echo "Error: Prompt file not found: $PROMPT"
        exit 1
    fi
    
    if [ ! -f "$METADATA" ]; then
        echo "Error: Metadata file not found: $METADATA"
        exit 1
    fi
    
    echo "Building $OUTPUT..."
    mkdir -p _build
    
    # Extract skills from metadata and create the list of input files
    SKILLS=$(yq -r '.skills[]' "$METADATA")
    
    # Build pandoc command with prompt + all skill files
    pandoc "$PROMPT" $SKILLS \
        --metadata-file="$METADATA" \
        --template="$TEMPLATE" \
        -o "$OUTPUT"
    
    echo "Build successful: $OUTPUT"

# Check for typos
typos:
    @echo "Checking for typos..."
    @typos

# Fix typos automatically
typos-fix:
    @echo "Fixing typos..."
    @typos --write-changes

# Clean all build artifacts
clean:
    @echo "Cleaning _build directory..."
    @rm -rf _build/*
    @echo "Clean complete"

# Show available recipes
help:
    @just --list
