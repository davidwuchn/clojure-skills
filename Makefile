_build/%.md: prompts/%.md
	pandoc \
	--metadata-file prompts/$*.md \
	-f markdown -t markdown \
	$(shell echo prompts/$*.md && yq '.sections[]' prompts/$*.md) \
	-o $@
	@echo "build successful: $@"
