package roy.ij.postofficesaathi.domain.pdf

object PdfPlacementSnapper {
    private const val SnapThreshold = 0.012f
    private const val MoveAwayEpsilon = 0.0001f

    data class Guides(
        val vertical: List<Float> = emptyList(),
        val horizontal: List<Float> = emptyList()
    ) {
        val hasGuides: Boolean = vertical.isNotEmpty() || horizontal.isNotEmpty()
    }

    data class ActiveSnap(
        val vertical: AxisSnap? = null,
        val horizontal: AxisSnap? = null,
        val suppressedVertical: AxisSnap? = null,
        val suppressedHorizontal: AxisSnap? = null
    ) {
        val hasSnap: Boolean = vertical != null || horizontal != null
    }

    data class Result(
        val placement: PdfImagePlacement,
        val guides: Guides,
        val isSnapped: Boolean,
        val activeSnap: ActiveSnap
    )

    data class AxisSnap(
        val anchorIndex: Int,
        val target: Float
    )

    private data class Anchor(val index: Int, val value: Float)

    private data class SnapMatch(
        val anchorIndex: Int,
        val anchor: Float,
        val target: Float,
        val distance: Float
    ) {
        fun toAxisSnap(): AxisSnap = AxisSnap(anchorIndex, target)
    }

    private data class AxisResult(
        val match: SnapMatch?,
        val suppressedSnap: AxisSnap? = null
    )

    fun snapPlacement(
        placement: PdfImagePlacement,
        placements: List<PdfImagePlacement>,
        activeIndex: Int,
        previousRawPlacement: PdfImagePlacement? = null,
        activeSnap: ActiveSnap = ActiveSnap()
    ): Result {
        var next = placement
        val verticalCandidates = mutableListOf(0.5f)
        val horizontalCandidates = mutableListOf(0.5f)
        placements.forEachIndexed { index, other ->
            if (index != activeIndex) {
                verticalCandidates += listOf(other.x, other.x + other.width / 2f, other.x + other.width)
                horizontalCandidates += listOf(other.y, other.y + other.height / 2f, other.y + other.height)
            }
        }

        val xAnchors = xAnchors(next)
        val yAnchors = yAnchors(next)
        val previousXAnchors = previousRawPlacement?.let(::xAnchors).orEmpty()
        val previousYAnchors = previousRawPlacement?.let(::yAnchors).orEmpty()

        val verticalResult = nearestSnap(
            anchors = xAnchors,
            targets = verticalCandidates,
            previousAnchors = previousXAnchors,
            activeSnap = activeSnap.vertical,
            suppressedSnap = activeSnap.suppressedVertical
        )
        val horizontalResult = nearestSnap(
            anchors = yAnchors,
            targets = horizontalCandidates,
            previousAnchors = previousYAnchors,
            activeSnap = activeSnap.horizontal,
            suppressedSnap = activeSnap.suppressedHorizontal
        )
        val verticalSnap = verticalResult.match
        val horizontalSnap = horizontalResult.match

        verticalSnap?.let { snap ->
            next = next.copy(x = next.x + snap.target - snap.anchor)
        }
        horizontalSnap?.let { snap ->
            next = next.copy(y = next.y + snap.target - snap.anchor)
        }

        val nextActiveSnap = ActiveSnap(
            vertical = verticalSnap?.toAxisSnap(),
            horizontal = horizontalSnap?.toAxisSnap(),
            suppressedVertical = verticalResult.suppressedSnap,
            suppressedHorizontal = horizontalResult.suppressedSnap
        )

        return Result(
            placement = next.clamped(),
            guides = Guides(
                vertical = verticalSnap?.let { listOf(it.target) }.orEmpty(),
                horizontal = horizontalSnap?.let { listOf(it.target) }.orEmpty()
            ),
            isSnapped = nextActiveSnap.hasSnap,
            activeSnap = nextActiveSnap
        )
    }

    private fun xAnchors(placement: PdfImagePlacement): List<Anchor> =
        listOf(
            Anchor(0, placement.x),
            Anchor(1, placement.x + placement.width / 2f),
            Anchor(2, placement.x + placement.width)
        )

    private fun yAnchors(placement: PdfImagePlacement): List<Anchor> =
        listOf(
            Anchor(0, placement.y),
            Anchor(1, placement.y + placement.height / 2f),
            Anchor(2, placement.y + placement.height)
        )

    private fun nearestSnap(
        anchors: List<Anchor>,
        targets: List<Float>,
        previousAnchors: List<Anchor>,
        activeSnap: AxisSnap?,
        suppressedSnap: AxisSnap?
    ): AxisResult {
        val matches = anchors
            .flatMap { anchor -> targets.map { target -> anchor to target } }
            .map { (anchor, target) ->
                SnapMatch(
                    anchorIndex = anchor.index,
                    anchor = anchor.value,
                    target = target,
                    distance = kotlin.math.abs(anchor.value - target)
                )
            }
            .filter { it.distance < SnapThreshold }

        val activeMatch = activeSnap?.let { snap ->
            matches.firstOrNull { it.matches(snap) }
        }
        if (activeMatch != null && activeMatch.isMovingAwayFrom(previousAnchors)) {
            val nextMatch = matches
                .filterNot { it.matches(activeMatch.toAxisSnap()) }
                .minByOrNull { it.distance }
            return AxisResult(match = nextMatch, suppressedSnap = activeMatch.toAxisSnap().takeIf { nextMatch == null })
        }

        val filteredMatches = matches.filterNot { match ->
            suppressedSnap != null &&
                match.matches(suppressedSnap) &&
                !match.isMovingToward(previousAnchors)
        }
        return AxisResult(
            match = filteredMatches.minByOrNull { it.distance },
            suppressedSnap = suppressedSnap?.takeIf { suppressed ->
                matches.any { it.matches(suppressed) } &&
                    filteredMatches.none { it.matches(suppressed) }
            }
        )
    }

    private fun SnapMatch.matches(snap: AxisSnap): Boolean =
        anchorIndex == snap.anchorIndex && target == snap.target

    private fun SnapMatch.isMovingAwayFrom(previousAnchors: List<Anchor>): Boolean {
        val previousAnchor = previousAnchors.firstOrNull { it.index == anchorIndex } ?: return false
        val previousDistance = kotlin.math.abs(previousAnchor.value - target)
        return distance > previousDistance + MoveAwayEpsilon
    }

    private fun SnapMatch.isMovingToward(previousAnchors: List<Anchor>): Boolean {
        val previousAnchor = previousAnchors.firstOrNull { it.index == anchorIndex } ?: return true
        val previousDistance = kotlin.math.abs(previousAnchor.value - target)
        return distance < previousDistance - MoveAwayEpsilon
    }
}
