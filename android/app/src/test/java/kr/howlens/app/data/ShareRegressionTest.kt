package kr.howlens.app.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Uses synthetic URLs and evidence; no live document source is accessed. */
class ShareRegressionTest {
    private fun previewFor(url: String): String {
        val evidence = Evidence(
            evidenceId = "evidence-test",
            documentId = "manual-test",
            documentVersion = "synthetic",
            pdfPage = 1,
            printedPage = null,
            section = "Synthetic test fixture",
            quote = "Synthetic test fixture",
            sourceUrl = url
        )
        val analysis = Analysis(
            analysisId = "analysis-test",
            deviceId = "server",
            decision = Decision.NEEDS_MORE_INFORMATION,
            observations = emptyList(),
            evidence = listOf(evidence),
            preconditions = emptyList(),
            steps = emptyList(),
            warnings = emptyList(),
            missingInformation = emptyList(),
            mode = Mode.LIVE
        )
        return SharePreview.build(analysis)
    }

    @Test
    fun percentEncodedCredentialQueryKeyIsOmitted() {
        val preview = previewFor("https://manual.example/doc?%74oken=SYNTHETIC_SECRET")

        assertFalse(preview.contains("SYNTHETIC_SECRET"))
        assertFalse(preview.contains("manual.example/doc"))
    }

    @Test
    fun credentialInFragmentIsOmitted() {
        val preview = previewFor("https://manual.example/doc#access_token=SYNTHETIC_SECRET")

        assertFalse(preview.contains("SYNTHETIC_SECRET"))
        assertFalse(preview.contains("manual.example/doc"))
    }

    @Test
    fun publicQueryAndAnchorRemainShareable() {
        val url = "https://manual.example/doc?chapter=4#section-2"
        val preview = previewFor(url)

        assertTrue(preview.contains(url))
    }
}
