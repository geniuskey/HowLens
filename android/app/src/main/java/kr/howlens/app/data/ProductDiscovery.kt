package kr.howlens.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URI
import java.time.Instant

@Serializable enum class DiscoveryStatus {
    @SerialName("candidate") CANDIDATE,
    @SerialName("needs_more_information") NEEDS_MORE_INFORMATION,
    @SerialName("not_found") NOT_FOUND,
}
@Serializable data class ProductSource(val title: String, val url: String,
    @SerialName("retrieved_at") val retrievedAt: String)
@Serializable data class ProductCandidate(val manufacturer: String, val model: String,
    val summary: String, @SerialName("match_notes") val matchNotes: List<String>,
    val sources: List<ProductSource>)
@Serializable data class ProductDiscovery(@SerialName("discovery_id") val discoveryId: String,
    val status: DiscoveryStatus, val candidates: List<ProductCandidate>,
    @SerialName("missing_information") val missingInformation: List<String>, val mode: Mode)

/** Discovery is product information only and never becomes a guide or catalog device ID. */
fun ProductDiscovery.requireValid(): ProductDiscovery = also {
    require(discoveryId.isNotBlank() && candidates.size <= 3)
    require(status != DiscoveryStatus.CANDIDATE || candidates.isNotEmpty())
    require(status == DiscoveryStatus.CANDIDATE || candidates.isEmpty())
    candidates.forEach { candidate ->
        require(candidate.manufacturer.isNotBlank() && candidate.model.isNotBlank())
        require(candidate.sources.size in 1..3)
        candidate.sources.forEach { source ->
            require(source.title.isNotBlank() && isPublicProductSource(source.url))
            Instant.parse(source.retrievedAt)
        }
    }
}

internal fun isPublicProductSource(value: String): Boolean = runCatching {
    val uri = URI(value)
    val host = uri.host?.lowercase()?.removeSuffix(".") ?: return false
    uri.scheme in setOf("https", "http") && uri.userInfo == null && uri.fragment == null &&
        '.' in host && ':' !in host && host.any { it in 'a'..'z' } &&
        !host.endsWith(".localhost") && !host.endsWith(".local") && !host.endsWith(".internal") &&
        !host.endsWith(".lan") && !host.endsWith(".home") &&
        !(uri.rawQuery.orEmpty().split('&', ';').any {
            val key = java.net.URLDecoder.decode(it.substringBefore('='), "UTF-8").lowercase()
            key in setOf("token", "access_token", "auth", "authorization", "key", "password", "secret", "signature", "sig")
        })
}.getOrDefault(false)
