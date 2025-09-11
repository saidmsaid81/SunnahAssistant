package com.thesunnahrevival.sunnahassistant.views.others

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.thesunnahrevival.sunnahassistant.BuildConfig
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.utilities.generateEmailIntent

class AboutAppFragment : Fragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SunnahAssistantTheme {
                    AboutAppScreen(
                        onUrlClick = { url -> openUrl(url) },
                        onContactClick = { openContactEmail() }
                    )
                }
            }
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun openContactEmail() {
        val intent = generateEmailIntent()
        startActivity(intent)
    }
}

@Composable
fun AboutAppScreen(
    onUrlClick: (String) -> Unit,
    onContactClick: () -> Unit
) {
    val context = LocalContext.current
    
    Surface {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = stringResource(R.string.icon),
                    modifier = Modifier.size(80.dp)
                )
            }
            
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary
                    )
                    
                    Text(
                        text = context.getString(R.string.version, BuildConfig.VERSION_NAME),
                        fontSize = 16.sp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            item {
                InfoSection(
                    content = stringResource(R.string.about),
                    onUrlClick = { url -> onUrlClick(url) }
                )
            }
            
            item {
                InfoSection(
                    title = stringResource(R.string.credits),
                    content = stringResource(R.string.app_icon_credit),
                    onUrlClick = { url -> onUrlClick(url) }
                )
            }
            
            item {
                InfoSection(
                    title = stringResource(R.string.translators),
                    content = stringResource(R.string.arabic_translation_by_malkalashter313),
                    onUrlClick = { url -> onUrlClick(url) }
                )
            }
            
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.we_are_social),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SocialMediaIcon(
                            iconRes = R.drawable.ic_web,
                            contentDescription = "Website Logo",
                            onClick = {
                                val urlWithTracking = "https://www.thesunnahrevival.com?" +
                                        "utm_source=${R.string.app_name}-Android-App" +
                                        "&utm_medium=social_icon" +
                                        "&utm_campaign=about_app"
                                onUrlClick(urlWithTracking)
                            }
                        )

                        SocialMediaIcon(
                            iconRes = R.drawable.ic_whatsapp,
                            contentDescription = "Whatsapp Logo",
                            onClick = { onUrlClick("https://whatsapp.com/channel/0029Va5ijm7DOQIV2hEgwu39") }
                        )
                        SocialMediaIcon(
                            iconRes = R.drawable.ic_facebook,
                            contentDescription = stringResource(R.string.facebook_logo),
                            onClick = { onUrlClick("https://www.facebook.com/thesunnahrevival") }
                        )
                        
                        SocialMediaIcon(
                            iconRes = R.drawable.ic_twitter,
                            contentDescription = stringResource(R.string.twitter_logo),
                            onClick = { onUrlClick("https://www.twitter.com/thesunahrevival") }
                        )
                        
                        SocialMediaIcon(
                            iconRes = R.drawable.ic_instagram,
                            contentDescription = stringResource(R.string.instagram_logo),
                            onClick = { onUrlClick("https://www.instagram.com/thesunnahrevival") }
                        )
                        
                        SocialMediaIcon(
                            iconRes = R.drawable.ic_telegram,
                            contentDescription = stringResource(R.string.telegram_logo),
                            onClick = { onUrlClick("https://t.me/thesunnahrevival") }
                        )
                    }
                }
            }
            
            item {
                Button(
                    onClick = onContactClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.contact_us),
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun InfoSection(
    title: String? = null,
    content: String,
    onUrlClick: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        title?.let {
            Text(
                text = it,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HtmlText(
                    html = content,
                    style = TextStyle(
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colors.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    onUrlClick = onUrlClick
                )
            }
        }
    }
}

@Composable
fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    onUrlClick: (String) -> Unit = {}
) {
    val annotatedString = parseHtmlToAnnotatedString(html, onUrlClick)
    
    Text(
        text = annotatedString,
        modifier = modifier,
        style = style
    )
}

@Composable
fun parseHtmlToAnnotatedString(html: String, onUrlClick: (String) -> Unit): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        val linkPattern = Regex("""<a href=([^>]+)>([^<]+)</a>""")
        val brPattern = Regex("""<br\s*/?>""")
        
        // Replace <br> tags and also handle existing \n characters
        val processedHtml = html.replace(brPattern, "\n")
            .replace("\\n", "\n") // Handle escaped newlines
        
        val matches = linkPattern.findAll(processedHtml)
        
        for (match in matches) {
            val beforeLink = processedHtml.substring(currentIndex, match.range.first)
            if (beforeLink.isNotEmpty()) {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colors.onSurface
                    )
                ) {
                    append(beforeLink)
                }
            }
            
            val url = match.groupValues[1]
            val linkText = match.groupValues[2]
            
            withLink(
                LinkAnnotation.Clickable(
                    tag = "URL",
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = MaterialTheme.colors.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                ) {
                    onUrlClick(url)
                }
            ) {
                append(linkText)
            }
            
            currentIndex = match.range.last + 1
        }
        
        if (currentIndex < processedHtml.length) {
            val remainingText = processedHtml.substring(currentIndex)
            if (remainingText.isNotEmpty()) {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colors.onSurface
                    )
                ) {
                    append(remainingText.trim())
                }
            }
        }
    }
}

@Composable
fun SocialMediaIcon(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(36.dp)
        )
    }
}