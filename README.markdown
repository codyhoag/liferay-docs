# LIFERAY-DOCS

<a href="https://dev.liferay.com" >
<img src="guidelines/images/liferayDeveloperNetworkSmallerEdit7.jpg" alt="Liferay Docs Icon">
</a>

Welcome to Liferay's official documentation project, the home of
[Liferay Developer Network](https://dev.liferay.com) articles. All articles are
written in
[Markdown](http://fletcher.github.com/peg-multimarkdown/mmd-manual.pdf), making
them easy to write and easy to read. Approved articles are uploaded to the
Liferay Developer Network (LDN) and converted automatically to HTML. In this
project, you can contribute new articles, improve existing articles, or fix
documentation bugs. To produce documentation that is comprehensive and
stylistically consistent, the liferay-docs project provides
[writing guidelines](guidelines/writers-guidelines.markdown),
[Markdown guidelines](guidelines/writers-guidelines.markdown), and a
[tutorial template](develop/tutorial-template.markdown).

This README contains the following sections:

- [Getting Started](#getting-started)
- [Structure](#structure)

You can start learning how to contribute by following the
[Getting Started](#getting-started) section next. 

## Getting Started

If you�re new to the liferay-docs repository and you want to add/edit material
locally, you first must clone the project. For information on how to work with
Git projects, see
[How do I use Git and GitHub?](guidelines/faq.markdown#how-do-i-use-git-and-github).

To contribute a new article, add it in an appropriate
[`new-articles`](new-articles) folder found in either the
[`discover`](discover), [`develop`](develop), or [`distribute`](distribute)
folder. The repository folders follow a [structure](#structure) similar to the
Liferay Developer Network's layout of pages and articles. To add an article to
LDN's
[Develop](https://dev.liferay.com/develop) section, for example, create your
article and its images in the `liferay-docs/develop/new-articles` folder. For
more information on where to place new articles and for guidelines on writing
them, see
[How do I write and submit my own article?](guidelines/faq.markdown#how-do-i-write-and-submit-my-own-article).

All articles should be written in Markdown. Converting your article to HTML
locally helps you ensure you�ve correctly formatted your Markdown text. You can
convert your Markdown to HTML by using one of the `convert.[bat|sh]` scripts
found in the [`bin`](bin/) folder. See
[How do I convert my local Markdown to HTML using the provided convert scripts?](guidelines/faq.markdown#how-do-i-convert-my-local-markdown-to-html-using-the-provided-convert-scripts)
for details on using the script.

If you�d like to modify an existing article, you can edit it in its current
repository location. After you�ve made your changes, commit them and submit a
GitHub pull request to the default user `liferay`. To learn how to commit
changes and submit pull requests, see
[How do I use Git and GitHub?](guidelines/faq.markdown#how-do-i-use-git-and-github). 

After you've submitted a pull request, Liferay�s documentation team reviews
your contribution. Approved changes are merged into the liferay-docs repo and
published to the [Liferay Developer Network](https://dev.liferay.com).

## Structure

All [Liferay Developer Network](https://dev.liferay.com) articles reside in
liferay-docs repository folders. The folders are laid out in a similar manner
to LDN's pages and articles. Each repository folder under `discover`, `develop`,
and `distribute` represents a section of LDN's articles. For example, the
`discover/portal` folder contains Markdown files and images that are the source
for LDN's
[Discover&rarr;Portal](https://dev.liferay.com/discover/portal) articles. 

To contribute new articles and images or modifications to LDN's 
[Discover](https://dev.liferay.com/discover),
[Develop](https://dev.liferay.com/develop), or
[Distribute](https://dev.liferay.com/distribute) sections, commit them to the
respective [`new-articles`](new-articles) folder. 

**Article Folders and Mappings to LDN Pages:**

 ![Discover](guidelines/images/discover.png)                                             | ![Develop](guidelines/images/develop.png)                                              | ![Distribute](guidelines/images/distribute.png) 
 :-------------------------------------------------------------------------------------- | :------------------------------------------------------------------------------------- | :--------------------------------------------
  &nbsp;[**discover/**](https://dev.liferay.com/discover)                                | &nbsp;[**develop/**](https://dev.liferay.com/develop)                                  | [**distribute/**](https://dev.liferay.com/distribute) 
  &nbsp;&#8226;&nbsp;[`deployment/`](https://dev.liferay.com/discover/deployment)        | &nbsp;&#8226;&nbsp;[`learning-paths/`](https://dev.liferay.com/develop/learning-paths) | &nbsp;&#8226;&nbsp;[`new-articles`](new-articles)
  &nbsp;&#8226;&nbsp;[`new-articles`](new-articles)                                      | &nbsp;&#8226;&nbsp;[`new-articles`](new-articles)                                      |
  &nbsp;&#8226;&nbsp;[`portal/`](https://dev.liferay.com/discover/portal)                | &nbsp;&#8226;&nbsp;[`reference/`](https://dev.liferay.com/develop/reference)           |
  &nbsp;&#8226;&nbsp;[`reference/`](https://dev.liferay.com/discover/reference)          | &nbsp;&#8226;&nbsp;[`tutorials/`](https://dev.liferay.com/develop/tutorials)           |
  &nbsp;&#8226;&nbsp;[`social-office/`](https://dev.liferay.com/discover/social-office)  |                                                                                        |
  
You've learned how to contirbute and you know the article folder structure. Got
questions? Check out the liferay-docs [FAQ](guidelines/faq.markdown).  

We look forward to your contributions to Liferay's official docs, here in the
liferay-docs project! 

