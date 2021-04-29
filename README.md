# RichLinkPreview
This library will provide seo meta data from the website

<!--
*** This library will provide seo meta data from the website. If you have a suggestion
*** that would make this better, please fork the repo and create a pull request
*** or simply open an issue with the tag "enhancement".
*** Thanks again! Now go create something AMAZING! :D
-->

<!-- PROJECT SHIELDS -->
<!--
*** I'm using JSOUP to parse HTML
*** https://github.com/jhy/jsoup
-->

<!-- PROJECT LOGO -->
<br />
<p align="center">
  <a href="https://avatars.githubusercontent.com/u/29120548?s=400&u=3d7b9aafe434f62a3f0c6d323be97195682917c6&v=4">
    <img src="https://avatars.githubusercontent.com/u/29120548?s=400&u=3d7b9aafe434f62a3f0c6d323be97195682917c6&v=4" alt="Abhijeet" width="80" height="80">
  </a>

  <h3 align="center">SEO-Rich Link Preview</h3>


<!-- GETTING STARTED -->
## Getting Started
### Prerequisites

Add the JitPack repository to your build file
* Step 1.  Add it in your root build.gradle at the end of repositories:
  ```sh
  allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  ```
  
* Step 2. Add the dependency:
  ```sh
  dependencies {
	        implementation 'com.github.abhii2028:RichLinkPreview:0.0.1'
	}
  ```

* Step 3. Implement the Listener SeoPreview.ResponseListener:
  ```sh
    class YourClass : SeoPreview.ResponseListener {
      .....
    }
  ```

* Step 4. Initialize the Seo Preview class in Activity/Fragment:
  ```sh
    val seoPreview = SeoPreview(this /*Pass the listener*/ )
  ```

* Step 5. Call getPreview/getPreviews method:
  ```sh
    /*For single link*/
    seoPreview.getPreview("your url") { 
        //on complete callback
        //Data will come from onData(seo: SeoData), as this call back will be helpful to stop progress loader
    }
    /*For multiple links*/
    val urls = arrayListOf("url1","url2")
    seoPreview.getPreviews(urls) { 
        //on complete callback
        //Data will come from onData(seo: SeoData), as this call back will be helpful to stop progress loader
    }
  ```
* Step 6. Receive Data:
  ```sh
    //on link preview success
    override fun onData(seo: SeoData) {
        //add this data to your list item or any custom view
    }
  ```

* Step 7. See the magic:
  ```sh
    https://photos.app.goo.gl/ozwPhui1sJJrWB739
  ```



<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to be learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<!-- CONTACT -->
## Contact

Name - Abhijeet Prusty

Email - abhijeetprusty28@gmail.com

Project Link: [https://github.com/abhii2028/RichLinkPreview)
