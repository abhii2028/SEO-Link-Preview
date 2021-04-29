package com.rich.link.seo

import android.webkit.URLUtil
import androidx.core.net.toUri
import com.rich.link.seo.SeoPreview.ResponseListener
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Created by Abhijeet Prusty
 * Fetch data from the html
 * @param responseListener : [ResponseListener]
 */
class SeoPreview(private val responseListener: ResponseListener) {

    private val tag = this::class.java.simpleName

    /**
     * Data Class
     */
    class SeoData {
        var thumb: String = ""
        var path: String = ""
        var title: String = ""
        var description: String = ""
        var siteName: String = ""
        var favIcon: String = ""
    }

    /**
     * Make any data type into [String] data type
     */
    val Any?.makeString: String
        get() {
            return when (this) {
                this == null -> ""
                else -> {
                    val text = toString()
                    if (text.equals("null", true)) "" else text.trim()
                }
            }
        }

    /**
     * Send response call back
     */
    interface ResponseListener {
        fun onData(seo: SeoData)
        fun onError(error: String) {}
    }

    /**
     * Get preview from single url
     */
    fun getPreview(url: String?, complete: () -> Unit = {}) {
        val dataUrl = url.makeString
        if (dataUrl.isNotEmpty()) {
            getData(dataUrl, complete)
        } else {
            complete.invoke()
        }
    }

    /**
     * Get previews from the url list
     */
    fun getPreviews(urls: ArrayList<String>, complete: () -> Unit) {
        if (urls.isNotEmpty()) {
            val url = urls[0]
            println("URL process -> $url")
            //get first url
            getPreview(urls[0]) {
                println("URL complete -> $url")
                //remove first
                urls.removeAt(0)
                //check if more url remain
                if (urls.isNotEmpty()) {
                    getPreviews(urls, complete)
                } else {
                    complete.invoke()
                }
            }
        }
    }

    /**
     * Execute data fetching
     * @param url : [String]
     * @param complete : Call back
     */
    private fun getData(url: String, complete: () -> Unit) {
        //get meta data
        val seoMedia = SeoData().apply { path = url }

        //execute
        doAsync {
            //fetch data from network
            try {
                //Call network data
                val doc = Jsoup.connect(url)
                    .timeout(30 * 1000)
                    .get()

                //decode data
                decodeUsingOG(seoMedia, doc)

            } catch (e: Exception) {
                //remove url
                seoMedia.path = ""
                e.printStackTrace()
                //give call back on ui
                uiThread {
                    responseListener.onError(
                        "No Html Received from "
                                + url + " Check your Internet " + e.localizedMessage
                    )
                }
            }

            //after complete give call back on ui
            uiThread {
                //send call to get preview
                complete.invoke()
                //send data
                if (seoMedia.path.makeString.isNotEmpty() &&
                    seoMedia.thumb.makeString.isNotEmpty()
                ) responseListener.onData(seoMedia)
            }
        }
    }

    /**
     * Decode type
     */
    private object DecodeType {
        //content
        object Content {
            const val Meta = "meta"
            const val MetaData = "content"
            const val Link = "link"
            const val LinkData = "href"
            const val Main = "main"

            //Image
            object Image {
                const val Key = "img"
                const val Src = "src"
                const val SrcData = "data-src"
            }
        }

        //Keys
        object Key {
            const val FB = "og:"
            const val Twitter = "twitter:"

            //Inner Key
            object InnerKey {
                const val Property = "property="
                const val Name = "name="
                const val Relation = "rel="
            }
        }

        //Type //this will check the type of data //don't consider this as key
        enum class Type { Title, Desc, Image, FavIcon, Site }
    }

    /**
     * Method to get key for
     * @param type : [DecodeType.Type]
     */
    private fun getKeys(type: DecodeType.Type): ArrayList<String> {
        //empty list
        val keys = ArrayList<String>()
        //add keys
        when (type) {
            DecodeType.Type.Title -> {
                //FB
                keys.add(DecodeType.Key.InnerKey.Property + DecodeType.Key.FB + "title")
                keys.add(DecodeType.Key.InnerKey.Name + DecodeType.Key.FB + "title")
                //Twitter
                keys.add(DecodeType.Key.InnerKey.Property + DecodeType.Key.Twitter + "title")
                keys.add(DecodeType.Key.InnerKey.Name + DecodeType.Key.Twitter + "title")
            }
            DecodeType.Type.Desc -> {
                //FB
                keys.add(DecodeType.Key.InnerKey.Property + DecodeType.Key.FB + "description")
                keys.add(DecodeType.Key.InnerKey.Name + DecodeType.Key.FB + "description")
                //Twitter
                keys.add(DecodeType.Key.InnerKey.Property + DecodeType.Key.Twitter + "description")
                keys.add(DecodeType.Key.InnerKey.Name + DecodeType.Key.Twitter + "description")
            }
            DecodeType.Type.Image -> {
                //FB
                keys.add(DecodeType.Key.InnerKey.Property + DecodeType.Key.FB + "image")
                keys.add(DecodeType.Key.InnerKey.Name + DecodeType.Key.FB + "image")
                keys.add(DecodeType.Key.InnerKey.Property + DecodeType.Key.FB + "image:src")
                keys.add(DecodeType.Key.InnerKey.Name + DecodeType.Key.FB + "image:src")
                //Twitter
                keys.add(DecodeType.Key.InnerKey.Property + DecodeType.Key.Twitter + "image")
                keys.add(DecodeType.Key.InnerKey.Name + DecodeType.Key.Twitter + "image")
                keys.add(DecodeType.Key.InnerKey.Property + DecodeType.Key.Twitter + "image:src")
                keys.add(DecodeType.Key.InnerKey.Name + DecodeType.Key.Twitter + "image:src")
                //Other
                keys.add(DecodeType.Key.InnerKey.Relation + "image_src")
            }
            DecodeType.Type.FavIcon -> {
                //other
                keys.add(DecodeType.Key.InnerKey.Relation + "apple-touch-icon")
                keys.add(DecodeType.Key.InnerKey.Relation + "icon")
            }
            DecodeType.Type.Site -> {
                //FB
                keys.add(DecodeType.Key.InnerKey.Property + DecodeType.Key.FB + "site")
                keys.add(DecodeType.Key.InnerKey.Name + DecodeType.Key.FB + "site")
                keys.add(DecodeType.Key.InnerKey.Property + DecodeType.Key.FB + "site_name")
                keys.add(DecodeType.Key.InnerKey.Name + DecodeType.Key.FB + "site_name")
                //Twitter
                keys.add(DecodeType.Key.InnerKey.Property + DecodeType.Key.Twitter + "site")
                keys.add(DecodeType.Key.InnerKey.Name + DecodeType.Key.Twitter + "site")
                keys.add(DecodeType.Key.InnerKey.Property + DecodeType.Key.Twitter + "site_name")
                keys.add(DecodeType.Key.InnerKey.Name + DecodeType.Key.Twitter + "site_name")
            }
        }
        //return keys
        return keys
    }

    /**
     * Decode html to model class
     * @param seoMedia : [SeoData]
     * @param document : [Document]
     */
    private fun decodeUsingOG(seoMedia: SeoData, document: Document) {
        //fetch elements
        //val elements = document.getElementsByTagName(DecodeType.Content.Meta)
        println("$tag DATA FETCHING for ${seoMedia.path}         ---------------------------------")

        //get Title---------------------------------------------------------------------------------
        val titleKeys = getKeys(DecodeType.Type.Title)
        //loop the titles
        for (title in titleKeys) {
            if (seoMedia.title.makeString.isEmpty()) {
                seoMedia.title = document.select("${DecodeType.Content.Meta}[$title]")
                    .attr(DecodeType.Content.MetaData)
                    .makeString
            } else {
                println("$tag TITLE FETCH at -> $title : ${seoMedia.title}")
                //terminate loop
                break
            }
        }
        //Set title of document
        if (seoMedia.title.makeString.isEmpty()) {
            println("$tag TITLE FETCH at -> Doc.Title : ${seoMedia.title}")
            seoMedia.title = document.title().makeString
        }

        //get Description---------------------------------------------------------------------------
        val descKeys = getKeys(DecodeType.Type.Desc)
        //loop the Description
        for (desc in descKeys) {
            if (seoMedia.description.makeString.isEmpty()) {
                seoMedia.description = document.select("${DecodeType.Content.Meta}[$desc]")
                    .attr(DecodeType.Content.MetaData)
                    .makeString
            } else {
                println("$tag DESC FETCH at -> $desc : ${seoMedia.description}")
                //terminate loop
                break
            }
        }

        //get Images--------------------------------------------------------------------------------
        val imageKeys = getKeys(DecodeType.Type.Image)
        //loop the images
        for (image in imageKeys) {
            if (seoMedia.thumb.makeString.isEmpty()) {
                //fetch data
                val query: String
                val queryData: String
                if (image.startsWith(DecodeType.Key.InnerKey.Relation)) {
                    query = "${DecodeType.Content.Link}[$image]"
                    queryData = DecodeType.Content.LinkData
                } else {
                    query = "${DecodeType.Content.Meta}[$image]"
                    queryData = DecodeType.Content.MetaData
                }
                //get data
                val data = document.select(query).attr(queryData).makeString
                //set thumb
                if (URLUtil.isValidUrl(data)) seoMedia.thumb = data
            } else {
                println("$tag IMAGE FETCH at -> $image : ${seoMedia.thumb}")
                //terminate loop
                break
            }
        }

        //get main content
        val mainElements = document.getElementsByTag(DecodeType.Content.Main)
        val imgElements = if (mainElements.isNotEmpty()) {
            mainElements[0].getElementsByTag(DecodeType.Content.Image.Key)
        } else {
            document.getElementsByTag(DecodeType.Content.Image.Key)
        }
        //get image resource
        if (imgElements.size > 0) {
            for (i in 0 until imgElements.size) {
                if (seoMedia.thumb.makeString.isEmpty()) {
                    //get src
                    val src = imgElements[i].attr(DecodeType.Content.Image.Src)
                    val dataSrc = imgElements[i].attr(DecodeType.Content.Image.SrcData)
                    //set thumb
                    when {
                        URLUtil.isValidUrl(src) -> seoMedia.thumb = src
                        URLUtil.isValidUrl(dataSrc) -> seoMedia.thumb = dataSrc
                    }
                } else {
                    println("$tag IMAGE FETCH at $i -> SRC MAIN : ${seoMedia.thumb}")
                    //terminate loop
                    break
                }
            }
        }

        //get Favicon-------------------------------------------------------------------------------
        val favKeys = getKeys(DecodeType.Type.FavIcon)
        //loop the Favicon
        for (fav in favKeys) {
            if (seoMedia.favIcon.makeString.isEmpty()) {
                //set favicon
                seoMedia.favIcon = document.select("${DecodeType.Content.Link}[$fav]")
                    .attr(DecodeType.Content.LinkData)
                    .makeString
            } else {
                println("$tag FAVICON FETCH at -> $fav : ${seoMedia.favIcon}")
                //terminate loop
                break
            }
        }

        //save fav icon in thumb if nothing is found
        if (seoMedia.thumb.makeString.isEmpty() && seoMedia.favIcon.makeString.isNotEmpty()) {
            if (URLUtil.isValidUrl(seoMedia.favIcon.makeString))
                seoMedia.thumb = seoMedia.favIcon
            else {
                val url = seoMedia.path.makeString
                val protocol = if (url.startsWith("http://")) "http://" else "https://"
                val urlFav = protocol + url.toUri().host + seoMedia.favIcon
                if (URLUtil.isValidUrl(urlFav)) {
                    seoMedia.thumb = urlFav
                }
            }

            println("$tag FAVICON FETCH at -> FavIcon : ${seoMedia.favIcon} : ${seoMedia.thumb}")
        }

        //get SiteName---------------------------------------------------------------------------
        val siteKeys = getKeys(DecodeType.Type.Site)
        //loop the SiteName
        for (site in siteKeys) {
            if (seoMedia.siteName.makeString.isEmpty()) {
                seoMedia.siteName = document.select("${DecodeType.Content.Meta}[$site]")
                    .attr(DecodeType.Content.MetaData)
                    .makeString
            } else {
                println("$tag SITE FETCH at -> $site : ${seoMedia.siteName}")
                //terminate loop
                break
            }
        }

        println("$tag DATA FETCHING Completed----------------------------------\n\n")
    }
}
