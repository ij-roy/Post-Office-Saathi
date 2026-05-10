package roy.ij.postofficesaathi.domain.pdf

enum class PdfLayoutType(
    val title: String,
    val description: String,
    val documentLabels: List<String>,
    val fileLabel: String
) {
    OneDocument(
        title = "1 card",
        description = "One card image on a clean A4 page.",
        documentLabels = listOf("Card 1"),
        fileLabel = "Document"
    ),
    TwoDocuments(
        title = "2 cards",
        description = "Two card images aligned with clear margins.",
        documentLabels = listOf("Card 1", "Card 2"),
        fileLabel = "Two_Documents"
    ),
    ThreeCards(
        title = "3 cards",
        description = "Three card images aligned on one PDF.",
        documentLabels = listOf("Card 1", "Card 2", "Card 3"),
        fileLabel = "Three_Cards"
    )
}
