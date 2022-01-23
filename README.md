# Lucilla

Lucilla is an in-memory Full Text Search library for Kotlin.

It allows to you to build a Full Text Search index for data that does not need to be persisted to a database.
You can run search queries against the index to find matching documents quickly.

```kotlin
import com.haroldadmin.lucilla.core.*

data class Book(
    @Id
    val id: Int,
    val title: String,
    val summary: String,
)

val index = useFts(getBooks())

index.search("Martian").map { searchResult ->
    val bookId = searchResult.documentId
    val book = getBook(bookId)
    // Show search result to the user
}
```

## Features

- PATRICIA Trie based space efficient FTS index
- Advanced text processing pipeline with support for Tokenization, Stemming, Punctuation removal and more.
- Extensible text processing with custom pipeline steps
- Search results ranking using [TF-IDF](https://en.wikipedia.org/wiki/Tf%E2%80%93idf) scores 
- Customisable document parsing with ability to ignore unwanted fields

While lucilla has you covered on most of the basic features, support for some advanced features is missing (but planned):
- Fuzzy searching
- Custom field boosts
- Async processing

## Usage

### Modelling Data

To use lucilla's FTS capabilities, you must first model your data as a class. 

We recommend using data classes for this purpose, but anything should work as long as it satisfies the following requirements:
- Must have a `@Id` marked field that can be parsed as an `Int`
- Must have one or more other properties that can be parsed as `String`s

```kotlin
import com.haroldadmin.lucilla.core.Id

data class Book(
  @Id
  val id: Int,
  val title: String,
  val summary: String,
)
```

If you don't want lucilla to index some fields of your document, annotate them with `@Ignore`.

```kotlin
import com.haroldadmin.lucilla.core.Id
import com.haroldadmin.lucilla.core.Ignore

data class Book(
    @Id
    val id: Int,
    val title: String,
    val summary: String, 
    @Ignore
    val publisher: String,
)
```

### Create the Index

Create an FTS index and add your data to it:

```kotlin
val index = useFts<Book>()
getBooks().forEach { index.add(it) }

// You can also pass your seed data directly
val books = getBooks()
val index = useFts<Book>(books)
```

Adding documents to the index, or creating the index with seed data is a potentially expensive process depending on how large each document is.
It's best to perform this process on a background thread or Coroutine.

### Search the Index

Send your queries to the index to get search results ordered by relevance.

```kotlin
val searchResults = index.search(query)
val books = searchResults.map { r -> r.documentId }.map { id -> getBook(id) }
showResults(books)
```

Lucilla runs every search query through a text processing pipeline to extract searchable tokens from it. The tokens may not reflect the search query exactly. 
To find which token of your search query matched with a given search result, use the "matchTerm" property on a search result.

## Installation

Add the Jitpack repository to your list of repositories:

```groovy
// Project level build.gradle file
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

And then add the dependency in your gradle file:

```groovy
// Module build.gradle file
dependencies {
    implementation "com.github.haroldadmin.lucilla:core:(latest-version)"
}
```

## Contributing

lucilla is in active development and does not promise API stability. Expect the library to undergo significant changes before it reaches stable status.

We encourage the community to contribute features and report bugs.

## Meta

The name 'lucilla' is inspired from the name of [Sebastian Vettel's 2020 Ferrari](https://www.espn.in/f1/story/_/id/28890092/vettel-names-2020-car-lucilla).
It also sounds similar to _lucene_ (from Apache Lucene), which is the industry standard full text search framework.

lucilla's implementation borrows from a JavaScript library [MiniSearch](https://github.com/lucaong/minisearch).

## License

```text
MIT License

Copyright (c) 2022 Kshitij Chauhan

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
