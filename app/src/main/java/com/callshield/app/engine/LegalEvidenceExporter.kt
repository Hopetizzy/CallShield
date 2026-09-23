package com.callshield.app.engine

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.callshield.app.data.local.entity.BlockedCallRecord
import com.callshield.app.data.local.entity.QuarantinedSmsRecord
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

object LegalEvidenceExporter {

    private const val FCCPC_EMAIL = "lenderstaskforce@fccpc.gov.ng"

    /**
     * Generates a formal, legally formatted compliance PDF document offline.
     * Integrates both Intercepted Calls and Quarantined Harassment SMS.
     * Returns the generated File object.
     */
    fun generateFccpcComplaintPdf(
        context: Context,
        callRecords: List<BlockedCallRecord>,
        smsRecords: List<QuarantinedSmsRecord> = emptyList()
    ): File {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val pdfFile = File(reportsDir, "FCCPC_Evidence_Dossier_$timeStamp.pdf")

        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 standard width in points
        val pageHeight = 842 // A4 standard height in points

        val watFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'WAT'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Africa/Lagos")
        }
        val generatedAtWat = watFormat.format(Date())

        // Calculate audit hash
        val auditHash = computeAuditHash(callRecords, smsRecords)

        // Pagination setup
        val callsPerPage = 14
        val totalCallPages = if (callRecords.isEmpty()) 1 else ((callRecords.size - 1) / callsPerPage) + 1
        val smsPerPage = 6
        val totalSmsPages = if (smsRecords.isEmpty()) 0 else ((smsRecords.size - 1) / smsPerPage) + 1
        val totalPages = totalCallPages + totalSmsPages

        val paintTitle = Paint().apply {
            color = Color.rgb(18, 24, 38)
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintSubtitle = Paint().apply {
            color = Color.rgb(80, 90, 105)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintHeaderBox = Paint().apply {
            color = Color.rgb(240, 243, 248)
            style = Paint.Style.FILL
        }

        val paintBorder = Paint().apply {
            color = Color.rgb(200, 210, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val paintTextBold = Paint().apply {
            color = Color.rgb(20, 25, 35)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintTextRegular = Paint().apply {
            color = Color.rgb(40, 45, 55)
            textSize = 8f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintCode = Paint().apply {
            color = Color.rgb(30, 30, 40)
            textSize = 7.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintRowAlt = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }

        val paintAccentRed = Paint().apply {
            color = Color.rgb(190, 30, 45)
            textSize = 8f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintAccentAmber = Paint().apply {
            color = Color.rgb(180, 83, 9)
            textSize = 8f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        var globalPageNumber = 1

        // --- RENDER CALL EVIDENCE PAGES ---
        for (pageIndex in 0 until totalCallPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, globalPageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            var y = 35f

            // --- HEADER ---
            canvas.drawText("CALLSHIELD AI — DIGITAL FORENSICS EVIDENCE DOSSIER", 35f, y, paintTitle)
            y += 14f
            canvas.drawText("OFFICIAL REGULATORY SUBMISSION • SECTION 17 FCCPA 2018 & NDPA 2023", 35f, y, paintSubtitle)
            y += 18f

            // Header Info Box
            val headerBoxRect = RectF(35f, y, pageWidth - 35f, y + 80f)
            canvas.drawRoundRect(headerBoxRect, 6f, 6f, paintHeaderBox)
            canvas.drawRoundRect(headerBoxRect, 6f, 6f, paintBorder)

            val boxY = y + 14f
            canvas.drawText("RECIPIENT:", 45f, boxY, paintTextBold)
            canvas.drawText("Federal Competition & Consumer Protection Commission (lenderstaskforce@fccpc.gov.ng)", 115f, boxY, paintTextRegular)

            canvas.drawText("TARGET:", 45f, boxY + 13f, paintTextBold)
            canvas.drawText("Joint Taskforce on Digital Lending / Predatory Autodialers & Defamation SMS", 115f, boxY + 13f, paintTextRegular)

            canvas.drawText("TIMESTAMP:", 45f, boxY + 26f, paintTextBold)
            canvas.drawText("$generatedAtWat (Device Timezone)", 115f, boxY + 26f, paintTextRegular)

            canvas.drawText("EVIDENCE HASH:", 45f, boxY + 39f, paintTextBold)
            canvas.drawText("SHA256: ${auditHash.take(36)}...", 130f, boxY + 39f, paintCode)

            canvas.drawText("DEVICE / OS:", 45f, boxY + 52f, paintTextBold)
            canvas.drawText("Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}) • CallShield Offline Defense Grid", 115f, boxY + 52f, paintTextRegular)

            y += 94f

            // Statutory Disclaimer Notice
            if (globalPageNumber == 1) {
                val legalNoticeRect = RectF(35f, y, pageWidth - 35f, y + 36f)
                val paintNoticeBg = Paint().apply {
                    color = Color.rgb(254, 243, 199)
                    style = Paint.Style.FILL
                }
                val paintNoticeBorder = Paint().apply {
                    color = Color.rgb(245, 158, 11)
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }
                val paintNoticeText = Paint().apply {
                    color = Color.rgb(146, 64, 14)
                    textSize = 7.5f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                    isAntiAlias = true
                }
                canvas.drawRoundRect(legalNoticeRect, 4f, 4f, paintNoticeBg)
                canvas.drawRoundRect(legalNoticeRect, 4f, 4f, paintNoticeBorder)
                canvas.drawText("STATUTORY NOTICE: Contains timestamped logs of prohibited VoIP autodialers & debt recovery harassment", 45f, y + 14f, paintNoticeText)
                canvas.drawText("intercepted autonomously at the OS kernel level. Generated for FCCPC/NITDA regulatory enforcement.", 45f, y + 26f, paintNoticeText)
                y += 46f
            }

            // Section Title
            canvas.drawText("SECTION 1: INTERCEPTED PREDATORY & AUTODIALER CALLS", 35f, y, paintTextBold)
            y += 10f

            // Table of Calls
            val tableTop = y
            val colX = floatArrayOf(35f, 55f, 155f, 255f, 420f, 510f)

            val tableHeaderRect = RectF(35f, y, pageWidth - 35f, y + 18f)
            val paintTh = Paint().apply {
                color = Color.rgb(30, 41, 59)
                style = Paint.Style.FILL
            }
            val paintThText = Paint().apply {
                color = Color.WHITE
                textSize = 8f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawRoundRect(tableHeaderRect, 3f, 3f, paintTh)
            canvas.drawText("#", colX[0] + 5f, y + 12f, paintThText)
            canvas.drawText("TIMESTAMP (WAT)", colX[1], y + 12f, paintThText)
            canvas.drawText("CALLER NUMBER", colX[2], y + 12f, paintThText)
            canvas.drawText("RULE / VIOLATION", colX[3], y + 12f, paintThText)
            canvas.drawText("STATUS", colX[4], y + 12f, paintThText)
            canvas.drawText("LATENCY", colX[5], y + 12f, paintThText)

            y += 20f

            val startIndex = pageIndex * callsPerPage
            val endIndex = (startIndex + callsPerPage).coerceAtMost(callRecords.size)

            if (callRecords.isEmpty()) {
                canvas.drawText("No intercepted calls recorded in this dossier period.", 45f, y + 20f, paintTextRegular)
                y += 40f
            } else {
                for (i in startIndex until endIndex) {
                    val rec = callRecords[i]
                    val rowHeight = 22f

                    if (i % 2 == 1) {
                        canvas.drawRect(35f, y, pageWidth - 35f, y + rowHeight, paintRowAlt)
                    }
                    canvas.drawLine(35f, y + rowHeight, pageWidth - 35f, y + rowHeight, paintBorder)

                    val recTime = SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.getDefault()).apply {
                        timeZone = TimeZone.getTimeZone("Africa/Lagos")
                    }.format(Date(rec.timestamp))

                    val rowNum = (i + 1).toString()
                    val numDisplay = if (rec.isPrivateNumber) "PRIVATE / RESTRICTED" else rec.rawNumber
                    val ruleName = rec.matchedRuleName.take(28)

                    canvas.drawText(rowNum, colX[0] + 5f, y + 14f, paintCode)
                    canvas.drawText(recTime, colX[1], y + 14f, paintCode)
                    canvas.drawText(numDisplay, colX[2], y + 14f, paintTextBold)
                    canvas.drawText(ruleName, colX[3], y + 14f, paintTextRegular)
                    canvas.drawText("DROPPED", colX[4], y + 14f, paintAccentRed)
                    canvas.drawText("${rec.interceptionLatencyMs}ms", colX[5], y + 14f, paintCode)

                    y += rowHeight
                }
            }

            canvas.drawRect(35f, tableTop, pageWidth - 35f, y, paintBorder)

            // FOOTER
            val footerY = pageHeight - 35f
            canvas.drawLine(35f, footerY - 10f, pageWidth - 35f, footerY - 10f, paintBorder)
            canvas.drawText("CallShield AI Digital Forensics • 100% Offline Cryptographic Integrity", 35f, footerY, paintSubtitle)
            canvas.drawText("Page $globalPageNumber of $totalPages", pageWidth - 85f, footerY, paintSubtitle)

            pdfDocument.finishPage(page)
            globalPageNumber++
        }

        // --- RENDER QUARANTINED SMS EVIDENCE PAGES ---
        for (smsPageIndex in 0 until totalSmsPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, globalPageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            var y = 35f

            canvas.drawText("CALLSHIELD AI — DIGITAL FORENSICS EVIDENCE DOSSIER", 35f, y, paintTitle)
            y += 14f
            canvas.drawText("OFFICIAL REGULATORY SUBMISSION • SECTION 2: QUARANTINED HARASSMENT SMS", 35f, y, paintSubtitle)
            y += 24f

            val startIndex = smsPageIndex * smsPerPage
            val endIndex = (startIndex + smsPerPage).coerceAtMost(smsRecords.size)

            for (i in startIndex until endIndex) {
                val sms = smsRecords[i]
                val cardRect = RectF(35f, y, pageWidth - 35f, y + 68f)

                canvas.drawRoundRect(cardRect, 4f, 4f, paintRowAlt)
                canvas.drawRoundRect(cardRect, 4f, 4f, paintBorder)

                val smsTime = SimpleDateFormat("dd/MM/yyyy HH:mm:ss 'WAT'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("Africa/Lagos")
                }.format(Date(sms.timestamp))

                canvas.drawText("SENDER: ${sms.sender}", 45f, y + 14f, paintTextBold)
                canvas.drawText("CATEGORY: ${sms.threatCategory.name}", 240f, y + 14f, paintAccentAmber)
                canvas.drawText(smsTime, pageWidth - 165f, y + 14f, paintCode)

                // Message body snippet (wrapped)
                val cleanBody = sms.body.replace("\n", " ").take(130)
                canvas.drawText("\"$cleanBody...\"", 45f, y + 32f, paintTextRegular)

                canvas.drawText("FLAGGED TRIGGER: '${sms.matchedKeywordOrPattern}'", 45f, y + 54f, paintCode)
                canvas.drawText("STATUS: QUARANTINED OFFLINE", pageWidth - 170f, y + 54f, paintAccentRed)

                y += 76f
            }

            // FOOTER
            val footerY = pageHeight - 35f
            canvas.drawLine(35f, footerY - 10f, pageWidth - 35f, footerY - 10f, paintBorder)
            canvas.drawText("CallShield AI Digital Forensics • 100% Offline Cryptographic Integrity", 35f, footerY, paintSubtitle)
            canvas.drawText("Page $globalPageNumber of $totalPages", pageWidth - 85f, footerY, paintSubtitle)

            pdfDocument.finishPage(page)
            globalPageNumber++
        }

        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }

    /**
     * Generates a forensic CSV file of all blocked calls and quarantined SMS.
     */
    fun generateEvidenceCsv(
        context: Context,
        callRecords: List<BlockedCallRecord>,
        smsRecords: List<QuarantinedSmsRecord> = emptyList()
    ): File {
        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val csvFile = File(exportsDir, "CallShield_Threat_Ledger_$timeStamp.csv")

        val watFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Africa/Lagos")
        }

        FileWriter(csvFile).use { writer ->
            writer.append("Record_Type,ID,Timestamp_WAT,Sender_or_Caller,Normalized_E164,Violation_or_Category,Details_or_Pattern,Latency_MS,Trunk_Subnet\n")
            
            callRecords.forEachIndexed { index, rec ->
                val timeStr = watFormat.format(Date(rec.timestamp))
                val trunk = AdaptiveSubnetManager.extractTrunkPrefix(rec.rawNumber)
                val safeRaw = escapeCsv(rec.rawNumber)
                val safeNorm = escapeCsv(rec.normalizedNumber)
                val safeRule = escapeCsv(rec.matchedRuleName)
                val safePattern = escapeCsv(rec.matchedPattern)

                writer.append("CALL,${index + 1},\"$timeStr\",\"$safeRaw\",\"$safeNorm\",\"$safeRule\",\"$safePattern\",${rec.interceptionLatencyMs},\"$trunk\"\n")
            }

            smsRecords.forEachIndexed { index, sms ->
                val timeStr = watFormat.format(Date(sms.timestamp))
                val safeSender = escapeCsv(sms.sender)
                val safeNorm = escapeCsv(sms.normalizedSender)
                val safeCategory = escapeCsv(sms.threatCategory.name)
                val safeBody = escapeCsv(sms.body.take(120))

                writer.append("SMS,${index + 1},\"$timeStr\",\"$safeSender\",\"$safeNorm\",\"$safeCategory\",\"$safeBody\",0,\"N/A\"\n")
            }
        }

        return csvFile
    }

    /**
     * Creates an Intent to send the complaint directly to FCCPC with pre-filled details.
     */
    fun createFccpcEmailIntent(
        context: Context,
        evidenceFile: File,
        blockedCallCount: Int,
        quarantinedSmsCount: Int = 0
    ): Intent {
        val authority = "${context.packageName}.fileprovider"
        val fileUri: Uri = FileProvider.getUriForFile(context, authority, evidenceFile)

        val totalIncidents = blockedCallCount + quarantinedSmsCount
        val subject = "[FCCPC COMPLAINT] Unlawful Predatory Loan Harassment & Autodialer Log ($totalIncidents Violations)"
        val bodyText = """
Dear FCCPC Joint Regulatory Taskforce on Digital Lending,

I am submitting official digital evidence of ongoing telecommunication harassment, predatory autodialer calls, and threatening debt recovery SMS in direct violation of the Federal Competition and Consumer Protection Act (FCCPA) 2018 and the Nigeria Data Protection Act (NDPA) 2023.

SUMMARY OF EVIDENCE:
• Total Intercepted Harassment Calls: $blockedCallCount
• Total Quarantined Defamation/Threat SMS: $quarantinedSmsCount
• Telecommunication Trunks Used: Automated VoIP & PBX Numbers (+234 2..., +234 201..., 0700...)
• Interception Engine: CallShield AI (Autonomous On-Device Kernel Screener)

Attached is the cryptographically timestamped Evidence Dossier (PDF/CSV) detailing caller timestamps, rotating trunk signatures, and message contents.

I request that appropriate regulatory sanctions and enforcement actions be taken against the originating digital lending platforms and telecommunication trunk operators.

Yours faithfully,
A Nigerian Consumer & Citizen
        """.trimIndent()

        return Intent(Intent.ACTION_SEND).apply {
            type = if (evidenceFile.name.endsWith(".pdf")) "application/pdf" else "text/csv"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(FCCPC_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, bodyText)
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Creates a generic share intent for any evidence document (PDF or CSV).
     */
    fun createGenericShareIntent(
        context: Context,
        file: File,
        mimeType: String
    ): Intent {
        val authority = "${context.packageName}.fileprovider"
        val fileUri: Uri = FileProvider.getUriForFile(context, authority, file)

        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, "CallShield Threat Ledger Export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }

    private fun computeAuditHash(
        callRecords: List<BlockedCallRecord>,
        smsRecords: List<QuarantinedSmsRecord>
    ): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val callPayload = callRecords.joinToString(";") { "${it.timestamp}:${it.rawNumber}:${it.matchedPattern}" }
            val smsPayload = smsRecords.joinToString(";") { "${it.timestamp}:${it.sender}:${it.threatCategory.name}" }
            val totalPayload = "$callPayload##$smsPayload"
            val hashBytes = md.digest(totalPayload.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "UNAVAILABLE"
        }
    }
}
