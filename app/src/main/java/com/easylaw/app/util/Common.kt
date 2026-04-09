package com.easylaw.app.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import com.easylaw.app.data.models.common.FileUploadModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object Common {
    fun formatIsoDate(isoString: String): String =
        try {
            // 1. ISO_DATE_TIME 형식(Supabase 기본형)을 읽어들임
            val parsedDate = ZonedDateTime.parse(isoString)
            // 2. 원하는 형식(yyyy-MM-dd)으로 출력
            parsedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } catch (e: Exception) {
            // 파싱 실패 시 원본 혹은 빈 문자열 반환
            isoString.split("T")[0]
        }

    fun getAppVersion(context: Context): String =
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "알 수 없음"
        } catch (e: Exception) {
            "0.0.0"
        }

    // 파일을 업로드 할 떄는 로컬 주소를 가지고 정보를 반환한다.(로컬에서만 하는 작업임)
    fun getFileUploadModel(
        context: Context,
        uriString: String,
    ): FileUploadModel {
        val contentResolver = context.contentResolver
        val uri = Uri.parse(uriString)
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

        var fileName = ""
        var fileSize: Long = 0 // 용량을 담을 변수 (Byte 단위)

        try {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    // 1. 파일 이름 가져오기
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = it.getString(nameIndex)
                    }

                    // 2. 🌟 파일 용량 가져오기 (Byte)
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        fileSize = it.getLong(sizeIndex)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FileError", "파일 정보 추출 실패: ${e.message}")
        }

        if (fileName.isEmpty()) {
            fileName = uri.path?.substringAfterLast('/') ?: "unknown_file"
        }

        return FileUploadModel(
            uri = uriString,
            name = fileName,
            mimeType = mimeType,
            dataSize = formatFileSize(fileSize), // 3. 🌟 읽기 쉬운 단위로 변환해서 저장
        )
    }

    fun formatFileSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(sizeInBytes.toDouble()) / Math.log10(1024.0)).toInt()

        // 소수점 첫째 자리까지만 표시 (예: 1.5 MB)
        return String.format("%.1f %s", sizeInBytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    fun downloadFile(
        context: Context,
        url: String,
        fileName: String,
    ) {
        try {
            val request =
                DownloadManager
                    .Request(Uri.parse(url))
                    .setTitle(fileName)
                    .setDescription("파일을 다운로드 중입니다...")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Toast.makeText(context, "다운로드를 시작합니다.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("DownloadError", "다운로드 실패: ${e.message}")
            Toast.makeText(context, "다운로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 스트림 형태로 받아서 바이트배열로 반환
    fun getBytesFromUri(
        context: Context,
        uri: Uri,
    ): ByteArray = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: byteArrayOf()
}
