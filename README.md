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

## Version Matrix 
|       | Quarkus      | OpenSearch | OpenSearch Java Client |
|-------|--------------|------------|------------------------|
| 1.9.3 | 3.11.3       | 2.14.0     | 2.11.0                 |
| 1.9.2 | 3.11.3       | 2.14.0     | 2.10.4                 |
| 1.9.0 | 3.11.0       | 2.14.0     | 2.10.3                 |
| 1.8.4 | 3.10.2       | 2.14.0     | 2.11.0                 |
| 1.8.3 | 3.10.2       | 2.14.0     | 2.10.4                 |
| 1.8.1 | 3.10.2       | 2.14.0     | 2.10.3                 |
| 1.8.0 | 3.10.0       | 2.13.0     | 2.10.2                 |
| 1.7.4 | 3.9.5        | 2.14.0     | 2.11.0                 |
| 1.7.3 | 3.9.5        | 2.14.0     | 2.10.4                 |
| 1.7.1 | 3.9.5        | 2.14.0     | 2.10.3                 |
| 1.7.0 | 3.9.5        | 2.13.0     | 2.10.2                 |
| 1.6.8 | 3.8.5        | 2.14.0     | 2.11.0                 |
| 1.6.7 | 3.8.5        | 2.14.0     | 2.10.4                 |
| 1.6.5 | 3.8.4        | 2.14.0     | 2.10.3                 |
| 1.6.4 | 3.8.4        | 2.13.0     | 2.10.2                 |
| 1.6.3 | 3.8.3        | 2.12.0     | 2.9.1                  |
| 1.6.1 | 3.8.2        | 2.12.0     | 2.9.1                  |
| 1.6.0 | 3.8.2        | 2.12.0     | 2.9.0                  |
| 1.5.0 | 3.7.4        | 2.12.0     | 2.9.0                  |
| 1.4.1 | 3.6.9        | 2.12.0     | 2.9.0                  |
| 1.4.0 | 3.6.1        | 2.11.1     | 2.8.1                  |
| 1.3.1 | 3.5.3        | 2.11.1     | 2.8.1                  |
| 1.3.0 | 3.5.2        | 2.11.0     | 2.8.1                  |
| 1.2.7 | 3.2.10.Final | 2.12.0     | 2.9.0                  |
| 1.2.6 | 3.2.10.Final | 2.11.1     | 2.8.1                  |
| 1.2.5 | 3.2.9.Final  | 2.11.1     | 2.8.1                  |
| 1.2.3 | 3.2.8.Final  | 2.11.0     | 2.8.1                  |
| 1.2.2 | 3.2.4.Final  | 2.9.0      | 2.6.0                  |
| 1.2.1 | 3.2.3.Final  | 2.9.0      | 2.6.0                  |
| 1.2.0 | 3.2.0.Final  | 2.8.0      | 2.6.0                  |
| 1.0.4 | 3.1.2.Final  | 2.8.0      | 2.5.0                  |
| 1.0.3 | 3.1.1.Final  | 2.8.0      | 2.5.0                  |
| 1.0.2 | 3.1.0.Final  | 2.7.0      | 2.4.0                  |
| 1.0.1 | 3.1.0.Final  | 2.6.0      | 2.4.0                  |
| 1.0.0 | 3.0.0.Final  | 2.6.0      | 2.4.0                  |
| 0.2.6 | 2.16.6.Final | 2.6.0      | 2.2.0                  |
| 0.2.5 | 2.16.5.Final | 2.6.0      | 2.2.0                  |
| 0.2.4 | 2.16.4.Final | 2.6.0      | 2.2.0                  |
| 0.2.3 | 2.16.3.Final | 2.5.0      | 2.2.0                  |
| 0.2.2 | 2.16.2.Final | 2.5.0      | 2.2.0                  |
| 0.2.1 | 2.16.2.Final | 2.5.0      | 2.2.0                  |
| 0.2.0 | 2.15.2.Final | 2.4.1      | 2.2.0                  |

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
