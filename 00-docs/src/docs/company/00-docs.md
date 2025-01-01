# 00-docs
This provides a template or starting point for the documentation of your company project.

For documentation, we use [laika](https://typelevel.org/Laika/latest/table-of-content.html).

The following files you need to adjust:

```bash
00-docs/src/docs
    | - contact.md
    | - instructions.md
    | - pattern.md
    | - statistics.md
```

The following files will be created by `.helper.scala prepareDocs`:

```bash
00-docs/src/docs
    | - dependencies
    | - helium
    | - catalog.md
    | - devStatistics.md
    | - index.md
    | - overviewDependencies.md
    | - release.md
```
So do **not adjust** them manually.

