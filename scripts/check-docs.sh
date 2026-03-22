#!/usr/bin/env bash
# Documentation accuracy checker for fastjson3
# Validates that API references in docs match actual source code.
#
# Usage:
#   ./scripts/check-docs.sh          # check all
#   ./scripts/check-docs.sh --fix    # show suggested fixes
#
# This script catches the most common documentation errors:
#   - Non-existent enum values (ReadFeature, WriteFeature, NamingStrategy, etc.)
#   - Non-existent annotation parameters (@JSONField, @JSONType)
#   - Non-existent builder methods (ObjectMapper.Builder)
#   - Wrong class references (JSONReader, JSONWriter, SerializeFilter)
#   - Wrong method names (writeStartObject, writeFieldName, etc.)
#   - Broken internal links

set -euo pipefail

# Requires GNU grep (grep -P). On macOS: brew install grep, then use ggrep.
# CI runs on Linux where GNU grep is the default.

DOCS_DIR="docs"
SRC_DIR="core3/src/main/java/com/alibaba/fastjson3"
ERRORS=0

red()   { printf '\033[31m%s\033[0m\n' "$*"; }
green() { printf '\033[32m%s\033[0m\n' "$*"; }
yellow(){ printf '\033[33m%s\033[0m\n' "$*"; }

check_pattern() {
    local label="$1"
    local pattern="$2"
    local context="${3:-}"
    local matches
    matches=$(grep -rn --include='*.md' -P "$pattern" "$DOCS_DIR" 2>/dev/null \
        | grep -v 'internals/optimization' \
        | grep -v 'internals/architecture' \
        | grep -v '不支持' \
        | grep -v '不受支持' \
        | grep -v 'does not exist' \
        | grep -v 'not supported' \
        || true)
    if [[ -n "$matches" ]]; then
        red "FAIL: $label"
        echo "$matches" | head -20
        [[ -n "$context" ]] && yellow "  Hint: $context"
        echo ""
        ERRORS=$((ERRORS + 1))
    fi
}

check_not_in_fastjson3_context() {
    # Check pattern only in fastjson3 code blocks (not in "old API" migration context)
    local label="$1"
    local pattern="$2"
    local context="${3:-}"
    local matches
    # Exclude lines that are clearly fastjson 1.x/2.x "before" examples
    matches=$(grep -rn --include='*.md' -P "$pattern" "$DOCS_DIR" 2>/dev/null \
        | grep -v '===== fastjson 1' \
        | grep -v '===== fastjson2' \
        | grep -v '===== Jackson' \
        | grep -v 'Jackson 3.x' \
        | grep -v '===== Gson' \
        | grep -v '===== org.json' \
        | grep -v 'internals/optimization' \
        | grep -v 'internals/architecture' \
        | grep -v '不支持' \
        | grep -v '不受支持' \
        | grep -v '替代' \
        | grep -v '|.*|.*|' \
        | grep -v 'from-fastjson2' \
        || true)
    if [[ -n "$matches" ]]; then
        red "FAIL: $label"
        echo "$matches" | head -20
        [[ -n "$context" ]] && yellow "  Hint: $context"
        echo ""
        ERRORS=$((ERRORS + 1))
    fi
}

echo "========================================="
echo " fastjson3 Documentation Checker"
echo "========================================="
echo ""

# ---- 1. Non-existent enum values ----
echo "--- Checking enum values ---"

check_pattern "WriteEnumUsingOrdinal (does not exist)" \
    'WriteEnumUsingOrdinal' \
    "Only WriteEnumsUsingName and WriteEnumUsingToString exist"

check_pattern "ReadFeature.AllowComment (singular, should be AllowComments)" \
    'ReadFeature\.AllowComment(?!s)' \
    "Correct: ReadFeature.AllowComments"

check_pattern "AllowUnQuotedFieldNames (capital Q)" \
    'AllowUnQuoted' \
    "Correct: AllowUnquotedFieldNames (lowercase q)"

check_pattern "EscapeHtmlChars (does not exist)" \
    'EscapeHtmlChars' \
    "Correct: EscapeNoneAscii"

check_pattern "NamingStrategy.Never (should be NoneStrategy)" \
    'NamingStrategy\.Never[^a-zA-Z]' \
    "Correct: NamingStrategy.NoneStrategy"

check_pattern "NamingStrategy.ToLowerCamelCase (does not exist)" \
    'NamingStrategy\.ToLower' \
    "Correct: NamingStrategy.CamelCase"

check_pattern "NamingStrategy.UpperCase (does not exist, use UpperSnakeCase or UpperKebabCase)" \
    'NamingStrategy\.UpperCase[^S]' \
    "Use UpperSnakeCase or UpperKebabCase"

check_pattern "NotWriteArrayListClassName / NotWriteHashSetClassName (do not exist)" \
    'NotWrite(ArrayList|HashSet)ClassName'

# ---- 2. Non-existent annotation parameters ----
echo "--- Checking annotation parameters ---"

check_pattern "@JSONField(unwrapped=...) does not exist" \
    '@JSONField\([^)]*unwrapped' \
    "unwrapped is not supported in fastjson3"

check_pattern "@JSONField(raw=...) does not exist" \
    '@JSONField\([^)]*\braw\s*=' \
    "raw is not a valid parameter"

check_pattern "@JSONField(using=...) does not exist" \
    '@JSONField\([^)]*\busing\s*=' \
    "Use serializeUsing or deserializeUsing"

check_pattern "@JSONField(writeEnumUsingName=...) does not exist" \
    '@JSONField\([^)]*writeEnumUsingName' \
    "Use WriteFeature.WriteEnumsUsingName instead"

check_pattern "@JSONType(serializer=...) does not exist" \
    '@JSONType\([^)]*\bserializer\s*=' \
    "Use @JSONField(serializeUsing=...) on fields instead"

check_pattern "@JSONType(deserializer=...) does not exist" \
    '@JSONType\([^)]*\bdeserializer\s*=' \
    "Use @JSONField(deserializeUsing=...) on fields instead"

check_pattern "@JSONType(format=...) does not exist" \
    '@JSONType\([^)]*\bformat\s*=' \
    "Use @JSONField(format=...) on fields instead"

check_pattern "@JSONType(ignore=...) boolean does not exist (use ignores)" \
    '@JSONType\([^)]*\bignore\s*=\s*(true|false)' \
    "Use @JSONType(ignores={...}) for field names"

# ---- 3. Non-existent builder methods ----
echo "--- Checking ObjectMapper.Builder methods ---"

check_pattern ".dateFormat() builder method does not exist" \
    '\.(dateFormat)\(' \
    "Use @JSONField(format=...) on fields instead"

# Note: references in comments like "不支持 .dateFormat()" are excluded by the filter above

check_pattern ".namingStrategy() builder method does not exist" \
    '\.namingStrategy\(' \
    "Use @JSONType(naming=...) on classes instead"

check_pattern ".registerModule() does not exist (use addReaderModule/addWriterModule)" \
    '\.registerModule\('

check_pattern ".readFeatures() / .writeFeatures() do not exist" \
    '\.(readFeatures|writeFeatures)\(' \
    "Use enableRead/disableRead/enableWrite/disableWrite"

check_pattern ".enableReader() does not exist (use .enableRead())" \
    '\.enableReader\(' \
    "Correct: .enableRead()"

check_pattern ".addMixin() lowercase i (should be addMixIn)" \
    '\.addMixin\(' \
    "Correct: .addMixIn() with capital I"

# ---- 4. Wrong class references ----
echo "--- Checking class references ---"

check_not_in_fastjson3_context "JSONReader.Feature used as fastjson3 API" \
    'JSONReader\.Feature' \
    "fastjson3 uses ReadFeature, not JSONReader.Feature"

check_not_in_fastjson3_context "JSONWriter.Feature used as fastjson3 API" \
    'JSONWriter\.Feature' \
    "fastjson3 uses WriteFeature, not JSONWriter.Feature"

# Note: from-fastjson2.md is excluded from the above because it inherently
# compares JSONReader.Feature/JSONWriter.Feature with ReadFeature/WriteFeature

check_pattern "SerializeFilter in fastjson3 context (does not exist)" \
    'extends SerializeFilter' \
    "Filter interfaces are standalone, no SerializeFilter base"

check_pattern "AutoTypeFilter class does not exist" \
    'AutoTypeFilter' \
    "Use @JSONType(seeAlso=...) for safe polymorphism instead"

check_pattern "@JSONField(writeUsing=...) does not exist (use serializeUsing)" \
    '@JSONField\([^)]*writeUsing' \
    "Correct: @JSONField(serializeUsing = ...)"

check_pattern "JSONSchema.of(String) does not exist (use parseSchema)" \
    'JSONSchema\.of\(\s*"' \
    "Correct: JSONSchema.parseSchema(string)"

check_pattern "JSONSchema.of(schemaJson/schemaStr) — likely wrong if argument is String" \
    'JSONSchema\.of\(schema(Json|Str|String)' \
    "If argument is String, use parseSchema(). of() only accepts JSONObject."

# ---- 5. Wrong JSONGenerator method names ----
# Note: migration/ is excluded because it shows other libraries' API on the "before" side
echo "--- Checking JSONGenerator methods ---"

check_generator_pattern() {
    local label="$1"
    local pattern="$2"
    local context="${3:-}"
    local matches
    matches=$(grep -rn --include='*.md' -P "$pattern" "$DOCS_DIR" 2>/dev/null \
        | grep -v 'internals/' \
        | grep -v 'migration/' \
        || true)
    if [[ -n "$matches" ]]; then
        red "FAIL: $label"
        echo "$matches" | head -20
        [[ -n "$context" ]] && yellow "  Hint: $context"
        echo ""
        ERRORS=$((ERRORS + 1))
    fi
}

check_generator_pattern "writeStartObject() does not exist (use startObject())" \
    'writeStartObject\(\)' \
    "Correct: startObject()"

check_generator_pattern "writeEndObject() does not exist (use endObject())" \
    'writeEndObject\(\)' \
    "Correct: endObject()"

check_generator_pattern "writeFieldName() does not exist (use writeName())" \
    'writeFieldName\(' \
    "Correct: writeName()"

check_generator_pattern "writeStringField() does not exist (use writeName + writeString)" \
    'writeStringField\(' \
    "Use gen.writeName(key) then gen.writeString(value)"

check_generator_pattern "writeIntField() does not exist (use writeName + writeInt32)" \
    'writeIntField\(' \
    "Use gen.writeName(key) then gen.writeInt32(value)"

# ---- 6. Non-existent method overloads ----
echo "--- Checking method signatures ---"

check_pattern "getString(key, default) two-arg overload does not exist" \
    'getString\([^)]+,\s*"[^"]*"\)' \
    "getString() only takes one argument, returns null if missing"

check_pattern "getIntValue(key, default) two-arg overload does not exist" \
    'getIntValue\([^)]+,\s*\d+\)' \
    "getIntValue() returns 0 if missing, no default-value overload"

# Check toJSONString with filter args — exclude migration/ (legitimately shows old API)
check_generator_pattern "JSON.toJSONString(obj, filter) does not exist" \
    'toJSONString\([^,]+,\s*(filter|Filter|[a-z]+Filter)' \
    "Use ObjectMapper.builder().addXxxFilter(filter).build().writeValueAsString(obj)"

# ---- 7. Wrong Maven groupId ----
echo "--- Checking Maven coordinates ---"

check_pattern "Wrong groupId for fastjson3 (should be com.alibaba.fastjson3)" \
    '<groupId>com\.alibaba</groupId>\s*\n?\s*<artifactId>fastjson3' \
    "Correct: <groupId>com.alibaba.fastjson3</groupId>"

# ---- 8. Broken links ----
echo "--- Checking markdown links ---"

BROKEN_LINKS_FILE=$(mktemp)
while IFS= read -r file; do
    dir=$(dirname "$file")
    while IFS= read -r match; do
        target=$(echo "$match" | grep -oP '\]\(\K[^)]+')
        # Skip external links
        [[ "$target" =~ ^https?:// ]] && continue
        [[ "$target" =~ ^mailto: ]] && continue
        # Strip anchor from target for file existence check
        target_file="${target%%#*}"
        [[ -z "$target_file" ]] && continue  # pure anchor link like #section
        # Resolve relative path
        resolved="$dir/$target_file"
        if [[ ! -e "$resolved" ]]; then
            red "BROKEN LINK: $file -> $target"
            echo "1" >> "$BROKEN_LINKS_FILE"
        fi
    done < <(grep -oP '\[([^\]]*)\]\(([^)#][^)]*)\)' "$file" 2>/dev/null || true)
done < <(find "$DOCS_DIR" -name '*.md' -type f)

LINK_ERRORS=$(wc -l < "$BROKEN_LINKS_FILE" 2>/dev/null || echo 0)
rm -f "$BROKEN_LINKS_FILE"
if [[ $LINK_ERRORS -gt 0 ]]; then
    ERRORS=$((ERRORS + LINK_ERRORS))
fi

# ---- Summary ----
echo ""
echo "========================================="
if [[ $ERRORS -eq 0 ]]; then
    green "ALL CHECKS PASSED"
else
    red "$ERRORS check(s) failed"
    echo ""
    echo "Fix these issues before merging documentation changes."
    exit 1
fi
