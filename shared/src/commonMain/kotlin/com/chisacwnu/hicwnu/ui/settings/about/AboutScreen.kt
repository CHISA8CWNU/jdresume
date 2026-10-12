/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chisacwnu.hicwnu.Destination
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.a11y_back
import hicwnu.shared.generated.resources.about_app_line1
import hicwnu.shared.generated.resources.about_app_line2
import hicwnu.shared.generated.resources.arrow_back_24px
import hicwnu.shared.generated.resources.cwnu_logo_about
import hicwnu.shared.generated.resources.item_about_app
import hicwnu.shared.generated.resources.item_privacy_policy
import hicwnu.shared.generated.resources.item_service_terms
import hicwnu.shared.generated.resources.label_version_prefix

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.item_about_app)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.arrow_back_24px),
                            contentDescription = stringResource(Res.string.a11y_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val uriHandler = LocalUriHandler.current
        val appVersionName: String = koinInject(named("AppVersionName"))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.cwnu_logo_about),
                    contentDescription = stringResource(Res.string.about_app_line1),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { uriHandler.openUri("https://www.cwnu.edu.cn/xywh/VIxt.htm") }
                )
                Spacer(Modifier.height(28.dp))
                Text(
                    text = stringResource(Res.string.about_app_line1),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable {
                        uriHandler.openUri("https://chisa8cwnu.github.io/scheduleforCWNU/")
                    }
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.about_app_line2),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(Res.string.label_version_prefix, appVersionName),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Developed by CHISA_CWNU.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))


                Text(
                    text = "And thanks to 富士山下、青花鱼、小满和稀饭频道-运营官。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                Text(
                    text = "感谢各位师大兄弟姐妹对本软件的支持。",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FooterLink(stringResource(Res.string.item_privacy_policy)) {
                    onNavigate(Destination.PrivacyPolicy)
                }
                Text(
                    text = " · ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FooterLink(stringResource(Res.string.item_service_terms)) {
                    onNavigate(Destination.ServiceTerms)
                }
            }
        }
    }
}

@Composable
private fun FooterLink(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clickable(onClick = onClick)
    )
}
