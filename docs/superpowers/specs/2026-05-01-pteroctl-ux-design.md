# Pteroctl UX Refactor Design

## Goal
Make `pteroctl upload` intuitive for non-experts. The default path should be `/plugins`, with a clear message stating the default and how to override it. Avoid breaking existing usage.

## Scope
- Enhance `upload` command UX only.
- Keep script in bash.
- Add a simple, explicit directory override flag.
- Improve help/usage text for upload.
- Optional future work: add aliases.

Out of scope:
- Rewriting in Python.
- Changing other commands or API behavior.

## Current Behavior
- `upload` expects `upload <file> [dir]`.
- If no dir, it defaults to `/`.
- Messaging does not clearly explain defaults.

## Proposed Behavior
### Upload defaults
- `./pteroctl upload myplugin.jar`:
  - Defaults directory to `/plugins`.
  - Prints: `Directory not given; defaulting to /plugins. Use --dir to select target directory.`

### Upload with explicit directory
- `./pteroctl upload myplugin.jar --dir /mods`
- `./pteroctl upload myplugin.jar -d /mods`

### Argument parsing rules
- For `upload` subcommand, support:
  - `upload <file> [dir]` (backward compatible)
  - `upload <file> --dir <dir>`
  - `upload <file> -d <dir>`
- If both `[dir]` and `--dir/-d` are provided, `--dir/-d` wins.
- If no directory is provided, default to `/plugins` and print a clear message.

### Help/usage changes
- Update usage block to show:
  - `upload <file> [dir]` (defaults to `/plugins`)
  - `upload <file> --dir <dir>`
  - Example lines showing defaulting message.

## Error Handling
- If file is missing or unreadable, keep current error behavior.
- If directory is empty string or missing for `--dir/-d`, exit with a helpful usage message.

## Implementation Notes
- Keep existing API logic for upload URLs.
- Adjust argument parsing in the `upload` case to accept flags without affecting global `getopts` usage.
- Avoid changing behavior for any other subcommands.

## Testing/Verification
- Manual commands:
  - `./pteroctl upload plugins/myplugin.jar` (defaults to `/plugins` + message)
  - `./pteroctl upload plugins/myplugin.jar /otherdir` (uses `/otherdir`)
  - `./pteroctl upload plugins/myplugin.jar --dir /otherdir` (uses `/otherdir`)
  - `./pteroctl upload plugins/myplugin.jar -d /otherdir` (uses `/otherdir`)

## Future Considerations
- Add aliases like `up`, `put`, or `upload-plugin` if needed later.
