# Quarkiverse - Quarkus OpenSearch
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->
[![version](https://img.shields.io/maven-central/v/io.quarkiverse.opensearch/quarkus-opensearch-parent)](https://repo1.maven.org/maven2/io/quarkiverse/opensearch/)
[![Build](https://github.com/quarkiverse/quarkus-opensearch/workflows/Build/badge.svg)](https://github.com/quarkiverse/quarkus-opensearch/actions?query=workflow%3ABuild)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This extension allows you to connect to an [OpenSearch cluster](https://opensearch.org/) using the clients provided by the OpenSearch project.

## Supported OpenSearch Clients
- [OpenSearch Java Client](https://opensearch.org/docs/latest/clients/java/)
- OpenSearch REST client (core client)
- [OpenSearch REST high-level client](https://opensearch.org/docs/latest/clients/java-rest-high-level/)

Since version 1.2.5 (LTS) and 1.3.1 support for the REST clients is deprecated, please use the Java Client instead.
The Java Client is not dependent on the REST client anymore and supports Apache HttpClient 5 Transport as well as AWS SDK2 Transport.

## New in 2.0.0

The next major release brings several new features and improvements:

- **SPI-based class loading** for 3rd party dependencies such as Apache HttpClient5 and AWS SDK2 Transport to reduce the dependency footprint and improve modularity.
- **Named client support**  
  Enables support for multiple OpenSearch client configurations.  
  This resolves [issue #334](https://github.com/quarkiverse/quarkus-opensearch/issues/334).
- **Support for Quarkus TLS Registry**  
  Integrates with the Quarkus TLS configuration registry to allow secure client setup via shared TLS contexts.

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
