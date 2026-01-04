# Quarkiverse - Quarkus OpenSearch
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->
[![version](https://img.shields.io/maven-central/v/io.quarkiverse.opensearch/quarkus-opensearch-parent)](https://repo1.maven.org/maven2/io/quarkiverse/opensearch/)
[![Build](https://github.com/quarkiverse/quarkus-opensearch/workflows/Build/badge.svg)](https://github.com/quarkiverse/quarkus-opensearch/actions?query=workflow%3ABuild)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This extension allows you to connect to an [OpenSearch cluster](https://opensearch.org/) using the clients provided by the OpenSearch project.

## Version Compatibility

| Extension | OpenSearch | Quarkus | JDK | Status |
|-----------|------------|---------|-----|--------|
| **3.x** | 3.x | 3.27 LTS+ | 21+ | Active |
| **2.x** | 2.x | 3.27 LTS | 17+ | Maintenance |

**Important:** Extension 3.x requires JDK 21 (OpenSearch 3.x requirement) and only supports the OpenSearch Java Client.

## Supported OpenSearch Clients

- [OpenSearch Java Client](https://opensearch.org/docs/latest/clients/java/) - **Recommended for all new projects**

The deprecated REST clients (low-level and high-level) have been removed in extension 3.x. Use extension 2.x if you need REST client support.

## New in 3.0.0

- **OpenSearch 3.x support** - Full compatibility with OpenSearch 3.x server and client libraries
- **JDK 21 required** - Minimum Java version increased to match OpenSearch 3.x requirements
- **OpenSearch Java Client only** - Deprecated REST clients removed (use extension 2.x for REST client support)

## New in 2.0.0

- **SPI-based class loading** for 3rd party dependencies such as Apache HttpClient5 and AWS SDK2 Transport to reduce the dependency footprint and improve modularity.
- **Named client support** - Enables support for multiple OpenSearch client configurations ([issue #334](https://github.com/quarkiverse/quarkus-opensearch/issues/334))
- **Support for Quarkus TLS Registry** - Integrates with the Quarkus TLS configuration registry to allow secure client setup via shared TLS contexts

## Latest Releases
For latest Release Information see:
https://github.com/quarkiverse/quarkus-opensearch/releases

## Documentation

Take a look at [the documentation](https://github.com/quarkiverse/quarkus-opensearch/blob/main/docs/modules/ROOT/pages/index.adoc) on how
to use the extension.

## Contributing

Contributions are always welcome, but better create an issue to discuss them prior to any contributions.

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):
<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center"><a href="https://github.com/sboeckelmann"><img src="https://avatars.githubusercontent.com/u/20949582?v=4?s=100" width="100px;" alt="sboeckelmann"/><br /><sub><b>sboeckelmann</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-opensearch/commits?author=sboeckelmann" title="Code">💻</a></td>
      <td align="center"><a href="https://www.loicmathieu.fr"><img src="https://avatars.githubusercontent.com/u/1819009?v=4?s=100" width="100px;" alt="Loïc Mathieu"/><br /><sub><b>Loïc Mathieu</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-opensearch/commits?author=loicmathieu" title="Code">💻</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!
