package com.github.yeriomin.playstoreapi;

import okhttp3.Headers;
import okhttp3.Request;
import okio.Buffer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class GooglePlayAPITest {

    private static final String EMAIL = "konstantin.razdolbaev@gmail.com";
    private static final String PASSWORD = "TemporaryPassword";
    private static final String GSFID = "3f1abe856b0fa7fd";
    private static final String TOKEN = "IwSyrLX3vSyMI1wdSUEdZ08EVcbg---1Wto-Owy7FP-U9eeRcwASB4z7p-Ne8ECDNp3FNw.";

    private MockGooglePlayAPI api;

    @Before
    public void setUp() throws Exception {
        api = initApi();
    }

    @Test
    public void getGsfId() throws Exception {
        MockGooglePlayAPI api = initApi();
        api.setGsfId(null);
        api.setToken(null);
        String gsfId = api.getGsfId(PASSWORD);
        Assert.assertEquals("34a77e79566015bf", gsfId);

        List<Request> requests = api.getRequests();
        Assert.assertEquals(3, requests.size());

        Request requestCheckin1 = requests.get(0);
        Assert.assertEquals(1, requestCheckin1.url().pathSegments().size());
        Assert.assertEquals("checkin", requestCheckin1.url().pathSegments().get(0));
        Assert.assertNull(requestCheckin1.header("Authorization"));
        Assert.assertNull(requestCheckin1.header("X-DFE-Device-Id"));
        Assert.assertEquals("Android-Finsky/7.1.15 (api=3,versionCode=80711500,sdk=22,device=C6902,hardware=qcom,product=C6902)", requestCheckin1.header("User-Agent"));
        Assert.assertEquals("en-US", requestCheckin1.header("Accept-Language"));
        AndroidCheckinRequest requestCheckinProto1 = AndroidCheckinRequest.parseFrom(MockOkHttpClientWrapper.getBodyBytes(requestCheckin1));
        Assert.assertEquals("C6902", requestCheckinProto1.getCheckin().getBuild().getDevice());

        Request requestAuthAc2dm = requests.get(1);
        Assert.assertEquals(1, requestAuthAc2dm.url().pathSegments().size());
        Assert.assertEquals("auth", requestAuthAc2dm.url().pathSegments().get(0));
        Map<String, String> vars = MockOkHttpClientWrapper.parseQueryString(MockOkHttpClientWrapper.getBodyBytes(requestAuthAc2dm));
        Assert.assertEquals(12, vars.size());
        Assert.assertEquals("konstantin.razdolbaev@gmail.com", vars.get("Email"));
        Assert.assertEquals("TemporaryPassword", vars.get("Passwd"));
        Assert.assertEquals("us", vars.get("device_country"));
        Assert.assertEquals("en", vars.get("lang"));
        Assert.assertEquals("22", vars.get("sdk_version"));
        Assert.assertEquals("ac2dm", vars.get("service"));
        Assert.assertEquals("1", vars.get("add_account"));
        Assert.assertEquals("com.google.android.gsf", vars.get("app"));

        Request requestCheckin2 = requests.get(2);
        Assert.assertEquals(1, requestCheckin2.url().pathSegments().size());
        Assert.assertEquals("checkin", requestCheckin2.url().pathSegments().get(0));
        Assert.assertNull(requestCheckin2.header("Authorization"));
        Assert.assertEquals("34a77e79566015bf", requestCheckin2.header("X-DFE-Device-Id"));
        Assert.assertEquals("Android-Finsky/7.1.15 (api=3,versionCode=80711500,sdk=22,device=C6902,hardware=qcom,product=C6902)", requestCheckin2.header("User-Agent"));
        Assert.assertEquals("en-US", requestCheckin2.header("Accept-Language"));
        AndroidCheckinRequest requestCheckinProto2 = AndroidCheckinRequest.parseFrom(MockOkHttpClientWrapper.getBodyBytes(requestCheckin2));
        Assert.assertEquals("C6902", requestCheckinProto2.getCheckin().getBuild().getDevice());
        Assert.assertEquals(3794140270688212415L, requestCheckinProto2.getId());
        Assert.assertEquals(7183672034703030426L, requestCheckinProto2.getSecurityToken());
        Assert.assertEquals("[konstantin.razdolbaev@gmail.com]", requestCheckinProto2.getAccountCookie(0));
        Assert.assertEquals("IwSyrMy-mgAWKCOUTIv1xWOVIu-IQPEv1c2fVQDgSMr_a_AfCARv8PTID0JG6r9FmnXFaA.", requestCheckinProto2.getAccountCookie(1));
    }

    @Test
    public void getToken() throws Exception {
        MockGooglePlayAPI api = initApi();
        api.setToken(null);
        String token = api.getToken(PASSWORD);
        Assert.assertEquals("IwSyrHW7GxO7BfDcjA1TNJ3tVzbgMFXCGYDSpLQNJ41VbHhZyTXslQTKlzjd7XXDSIBt6w.", token);

        List<Request> requests = api.getRequests();
        Assert.assertEquals(1, requests.size());

        Request request = requests.get(0);
        Assert.assertEquals(1, request.url().pathSegments().size());
        Assert.assertEquals("auth", request.url().pathSegments().get(0));
        Map<String, String> vars = MockOkHttpClientWrapper.parseQueryString(MockOkHttpClientWrapper.getBodyBytes(request));
        Assert.assertEquals(12, vars.size());
        Assert.assertEquals("konstantin.razdolbaev@gmail.com", vars.get("Email"));
        Assert.assertEquals("TemporaryPassword", vars.get("Passwd"));
        Assert.assertEquals("us", vars.get("device_country"));
        Assert.assertEquals("en", vars.get("lang"));
        Assert.assertEquals("22", vars.get("sdk_version"));
        Assert.assertEquals("androidmarket", vars.get("service"));
        Assert.assertEquals("3f1abe856b0fa7fd", vars.get("androidId"));
        Assert.assertEquals("com.android.vending", vars.get("app"));
    }

    @Test
    public void searchSuggest() throws Exception {
        SearchSuggestResponse response = api.searchSuggest("cp");

        List<Request> requests = api.getRequests();
        Assert.assertEquals(1, requests.size());
        Request request = requests.get(0);
        Assert.assertEquals(2, request.url().pathSegments().size());
        Assert.assertEquals("fdfe", request.url().pathSegments().get(0));
        Assert.assertEquals("searchSuggest", request.url().pathSegments().get(1));
        Assert.assertEquals(4, request.url().queryParameterNames().size());
        Assert.assertEquals("3", request.url().queryParameter("c"));
        Assert.assertEquals("cp", request.url().queryParameter("q"));
        Assert.assertEquals("2", request.url().queryParameter("sst"));
        Assert.assertEquals("120", request.url().queryParameter("ssis"));

        Assert.assertTrue(response.getEntryCount() > 0);
        SearchSuggestEntry appEntry = response.getEntry(0);
        Assert.assertEquals(2, appEntry.getType());
        SearchSuggestEntry suggestionEntry = response.getEntry(1);
        Assert.assertTrue(suggestionEntry.hasSuggestedQuery());
        Assert.assertEquals("cpu cooler", suggestionEntry.getSuggestedQuery());
    }

    @Test
    public void search() throws Exception {
        SearchResponse response = api.search("cpu");

        Assert.assertTrue(response.getDocCount() > 0);
        Assert.assertTrue(response.getDocList().get(0).hasContainerMetadata());
        Assert.assertTrue(response.getDocList().get(0).getContainerMetadata().hasNextPageUrl());
        Assert.assertEquals("clusterSearch?q=cpu&n=20&o=20&ecp=ggEFCgNjcHU%3D&ctntkn=-p6BnQMCCBQ%3D&fss=0&c=3&adsEnabled=1", response.getDocList().get(0).getContainerMetadata().getNextPageUrl());
        Assert.assertEquals(20, response.getDocList().get(0).getChildCount());
        DocV2 details = response.getDocList().get(0).getChild(0);
        Assert.assertEquals("CPU-Z", details.getTitle());

        List<Request> requests = api.getRequests();
        Assert.assertEquals(1, requests.size());
        Request request = requests.get(0);
        Assert.assertEquals(2, request.url().pathSegments().size());
        Assert.assertEquals("fdfe", request.url().pathSegments().get(0));
        Assert.assertEquals("search", request.url().pathSegments().get(1));
        Assert.assertEquals(2, request.url().queryParameterNames().size());
        Assert.assertEquals("3", request.url().queryParameter("c"));
        Assert.assertEquals("cpu", request.url().queryParameter("q"));
    }

    @Test
    public void getSearchIterator() throws Exception {
        GooglePlayAPI.SearchIterator i = api.getSearchIterator("cpu");
        Assert.assertEquals("cpu", i.getQuery());
        Assert.assertTrue(i.hasNext());

        SearchResponse response = i.next();
        Assert.assertTrue(response.getDocCount() > 0);
        Assert.assertTrue(response.getDocList().get(0).hasContainerMetadata());
        Assert.assertTrue(response.getDocList().get(0).getContainerMetadata().hasNextPageUrl());
        Assert.assertEquals("clusterSearch?q=cpu&n=20&o=20&ecp=ggEFCgNjcHU%3D&ctntkn=-p6BnQMCCBQ%3D&fss=0&c=3&adsEnabled=1", response.getDocList().get(0).getContainerMetadata().getNextPageUrl());
        Assert.assertEquals(20, response.getDocList().get(0).getChildCount());
        DocV2 details = response.getDocList().get(0).getChild(0);
        Assert.assertEquals("CPU-Z", details.getTitle());

        Assert.assertTrue(i.hasNext());
        SearchResponse response1 = i.next();
        Assert.assertTrue(response1.getDocCount() > 0);
        Assert.assertTrue(response1.getDocList().get(0).hasContainerMetadata());
        Assert.assertTrue(response1.getDocList().get(0).getContainerMetadata().hasNextPageUrl());
        Assert.assertEquals("clusterSearch?q=cpu&n=20&o=40&ecp=ggEFCgNjcHU%3D&ctntkn=-p6BnQMCCCg%3D&fss=0&c=3&adsEnabled=1", response1.getDocList().get(0).getContainerMetadata().getNextPageUrl());
        Assert.assertEquals(20, response1.getDocList().get(0).getChildCount());
        DocV2 details1 = response1.getDocList().get(0).getChild(0);
        Assert.assertEquals("AnTuTu Benchmark", details1.getTitle());
    }

    @Test
    public void details() throws Exception {
        DetailsResponse response = api.details("com.cpuid.cpu_z");

        DocV2 details = response.getDocV2();
        Assert.assertEquals("CPU-Z", details.getTitle());
        Assert.assertEquals(1, details.getOffer(0).getOfferType());
        Assert.assertEquals(21, details.getDetails().getAppDetails().getVersionCode());
        Assert.assertEquals(3, details.getChildCount());
        Assert.assertTrue(details.getChild(0).getBackendDocid().startsWith("similar_apps"));
        Assert.assertEquals(21, details.getChild(0).getChildCount());
        Assert.assertTrue(details.getChild(1).getBackendDocid().startsWith("users_also_installed"));
        Assert.assertEquals(20, details.getChild(1).getChildCount());
        Assert.assertTrue(details.getChild(2).getBackendDocid().startsWith("users_also_installed"));
        Assert.assertEquals(20, details.getChild(2).getChildCount());
        Assert.assertEquals("AIDA64", details.getChild(2).getChild(0).getTitle());

        List<Request> requests = api.getRequests();
        Assert.assertEquals(1, requests.size());
        Request request = requests.get(0);
        Assert.assertEquals(2, request.url().pathSegments().size());
        Assert.assertEquals("fdfe", request.url().pathSegments().get(0));
        Assert.assertEquals("details", request.url().pathSegments().get(1));
        Assert.assertEquals(1, request.url().queryParameterNames().size());
        Assert.assertEquals("com.cpuid.cpu_z", request.url().queryParameter("doc"));
    }

    @Test
    public void bulkDetails() throws Exception {
        BulkDetailsResponse response = api.bulkDetails(Arrays.asList("com.cpuid.cpu_z", "org.torproject.android"));

        DocV2 details1 = response.getEntryList().get(0).getDoc();
        Assert.assertEquals("CPU-Z", details1.getTitle());
        DocV2 details2 = response.getEntryList().get(1).getDoc();
        Assert.assertEquals("Orbot: Proxy with Tor", details2.getTitle());

        List<Request> requests = api.getRequests();
        Assert.assertEquals(1, requests.size());
        Request request = requests.get(0);
        Assert.assertEquals(2, request.url().pathSegments().size());
        Assert.assertEquals("fdfe", request.url().pathSegments().get(0));
        Assert.assertEquals("bulkDetails", request.url().pathSegments().get(1));
        BulkDetailsRequest protoRequest = BulkDetailsRequest.parseFrom(MockOkHttpClientWrapper.getBodyBytes(request));
        Assert.assertEquals("com.cpuid.cpu_z", protoRequest.getDocid(0));
        Assert.assertEquals("org.torproject.android", protoRequest.getDocid(1));
    }

    @Test
    public void browse() throws Exception {
        BrowseResponse response = api.browse();
        BrowseLink link = response.getCategoryList().get(0);
        Assert.assertEquals("Android Wear", link.getName());
        Assert.assertEquals("homeV2?cat=ANDROID_WEAR&c=3", link.getDataUrl());

        List<Request> requests = api.getRequests();
        Assert.assertEquals(1, requests.size());
        Request request = requests.get(0);
        Assert.assertEquals(2, request.url().pathSegments().size());
        Assert.assertEquals("fdfe", request.url().pathSegments().get(0));
        Assert.assertEquals("browse", request.url().pathSegments().get(1));
        Assert.assertEquals(1, request.url().queryParameterNames().size());
        Assert.assertEquals("3", request.url().queryParameter("c"));
    }

    @Test
    public void purchase() throws Exception {
        BuyResponse response = api.purchase("com.cpuid.cpu_z", 21, 1);

        Assert.assertTrue(response.getPurchaseStatusResponse().hasAppDeliveryData());
        Assert.assertEquals("08242190784708202273", response.getPurchaseStatusResponse().getAppDeliveryData().getDownloadAuthCookie(0).getValue());
        Assert.assertEquals("https://android.clients.google.com/market/download/Download?packageName=com.cpuid.cpu_z&versionCode=21&ssl=1&token=AOTCm0Qk6Xp0vLAIMTlHfvRcskbmSGugu7zaMbPzul5Ieu2bHAxD5QqKW6whNXp6dO9cid8x171TOLlpMXxAuJZYKl6JbeMUCphFLIBprvX4MX8CV79EOzVAeNiyT0qUOOKulE-5O-YFOM4jd8qkAR5ratStfwTar_eDlbyLQRZObbxVVGUdICUBftnAk-E-gj-mfuClbF-i5dkO_H22KvMrzOLkbsGlCjkmfOxWZvzvtw_LTW-pxD4WWE5_UxrxD6ktnnYnppzWmri687Ju3zkNXLpljvE7Nvr3KQuoW9eye93Wl2hSJSHIghoHQI7H3ImrCk8-gA2Cb-MGGapRne35_SZmjwAS&cpn=tQUzGf5FX6Y4LL14", response.getPurchaseStatusResponse().getAppDeliveryData().getDownloadUrl());

        List<Request> requests = api.getRequests();
        Assert.assertEquals(1, requests.size());
        Request request = requests.get(0);
        Assert.assertEquals(2, request.url().pathSegments().size());
        Assert.assertEquals("fdfe", request.url().pathSegments().get(0));
        Assert.assertEquals("purchase", request.url().pathSegments().get(1));
        Assert.assertEquals(0, request.url().queryParameterNames().size());

        Map<String, String> vars = MockOkHttpClientWrapper.parseQueryString(MockOkHttpClientWrapper.getBodyBytes(request));
        Assert.assertEquals(3, vars.size());
        Assert.assertEquals("com.cpuid.cpu_z", vars.get("doc"));
        Assert.assertEquals("1", vars.get("ot"));
        Assert.assertEquals("21", vars.get("vc"));
    }

    @Test
    public void delivery() throws Exception {
        DeliveryResponse responseFailure = api.delivery("com.mojang.minecraftpe", 871000016, 1);
        Assert.assertFalse(responseFailure.hasAppDeliveryData());

        DeliveryResponse response = api.delivery("com.cpuid.cpu_z", 21, 1);

        Assert.assertTrue(response.hasAppDeliveryData());
        Assert.assertEquals("08242190784708202273", response.getAppDeliveryData().getDownloadAuthCookie(0).getValue());
        Assert.assertEquals("https://android.clients.google.com/market/download/Download?packageName=com.cpuid.cpu_z&versionCode=21&ssl=1&token=AOTCm0RyRkIlbN-jMwP_CI5aTdDMK3wRaOXhz21bIvkVFDm90TyXrbt1YatiLK2LY1zoEU5iJ_u-AYBLnuAr69TiPAbBDfB6JtJGOVhrc1E2r9UDf70WnmKA_8s024v6g2ZJHCDi485u00NyosPHnCsnIfLI1hZSwqoAQ05bURLy72vFLBznWk_mTnjTy1HybRbxvWV_F4_Lw89Jmdr_RGKISl4bGpE943hE23T3Dy_unQKlZ81Q4i8CIfLEg0Dpl0T4KPXg0t8fiA1k4d0Xo3Mqudcyi3BeBBHS-lq4lS-Nx8RF09O92YsOuuFAHhSrGG0_NWNjd9N-4XuzRRJWjVvL1RtjyxUZ&cpn=wiHoRz1NXwwvhNPR", response.getAppDeliveryData().getDownloadUrl());

        List<Request> requests = api.getRequests();
        Assert.assertEquals(2, requests.size());
        Request request = requests.get(0);
        Assert.assertEquals(2, request.url().pathSegments().size());
        Assert.assertEquals("fdfe", request.url().pathSegments().get(0));
        Assert.assertEquals("delivery", request.url().pathSegments().get(1));
        Assert.assertEquals(3, request.url().queryParameterNames().size());
        Assert.assertEquals("com.mojang.minecraftpe", request.url().queryParameterValues("doc").get(0));
        Assert.assertEquals("1", request.url().queryParameterValues("ot").get(0));
        Assert.assertEquals("871000016", request.url().queryParameterValues("vc").get(0));
    }

    @Test
    public void reviews() throws Exception {
        ReviewResponse response = api.reviews("com.cpuid.cpu_z", GooglePlayAPI.REVIEW_SORT.HIGHRATING, 0, 20);

        Assert.assertTrue(response.getGetResponse().getReviewCount() > 0);
        Assert.assertEquals(5, response.getGetResponse().getReview(0).getStarRating());
        Assert.assertEquals("It is awesome app I install and it work success fully", response.getGetResponse().getReview(1).getComment());
        Assert.assertEquals("Nitesh Kumar", response.getGetResponse().getReview(0).getAuthor2().getName());
        Assert.assertEquals("https://plus.google.com/+NiteshKumar", response.getGetResponse().getReview(0).getAuthor2().getGooglePlusUrl());
        Assert.assertEquals("https://lh3.googleusercontent.com/-t-T8LKa60Fc/AAAAAAAAAAI/AAAAAAAAStk/wS6mDBhiWQA/photo.jpg", response.getGetResponse().getReview(0).getAuthor2().getUrls().getUrl());
        Assert.assertEquals("104245217570938637686", response.getGetResponse().getReview(0).getAuthor2().getPersonId());
        Assert.assertEquals("person-104245217570938637686", response.getGetResponse().getReview(0).getAuthor2().getPersonIdString());

        List<Request> requests = api.getRequests();
        Assert.assertEquals(1, requests.size());
        Request request = requests.get(0);
        Assert.assertEquals(2, request.url().pathSegments().size());
        Assert.assertEquals("fdfe", request.url().pathSegments().get(0));
        Assert.assertEquals("rev", request.url().pathSegments().get(1));
        Assert.assertEquals(5, request.url().queryParameterNames().size());
        Assert.assertEquals("com.cpuid.cpu_z", request.url().queryParameter("doc"));
        Assert.assertEquals("1", request.url().queryParameter("sort"));
        Assert.assertEquals("3", request.url().queryParameter("c"));
        Assert.assertEquals("0", request.url().queryParameter("o"));
        Assert.assertEquals("20", request.url().queryParameter("n"));
    }

    @Test
    public void addReview() throws Exception {
        ReviewResponse response = api.addOrEditReview("com.cpuid.cpu_z", "Работает!", "", 5);

        Assert.assertTrue(response.hasUserReview());
        Assert.assertEquals(5, response.getUserReview().getStarRating());
        Assert.assertEquals("Работает!", response.getUserReview().getComment());
        Assert.assertEquals("konstantin razdolbaev", response.getUserReview().getAuthor2().getName());
        Assert.assertEquals("", response.getUserReview().getAuthor2().getGooglePlusUrl());
        Assert.assertEquals("https://lh3.googleusercontent.com/-PkFVwXLKCKk/AAAAAAAAAAI/AAAAAAAAAAA/AKB_U8valX_uc0SKPSZEhVtxDUqYtRwIgQ/photo.jpg", response.getUserReview().getAuthor2().getUrls().getUrl());
        Assert.assertEquals("100687909122075437983", response.getUserReview().getAuthor2().getPersonId());
        Assert.assertEquals("person-100687909122075437983", response.getUserReview().getAuthor2().getPersonIdString());

        List<Request> requests = api.getRequests();
        Assert.assertEquals(1, requests.size());
        Request request = requests.get(0);
        Assert.assertEquals(2, request.url().pathSegments().size());
        Assert.assertEquals("fdfe", request.url().pathSegments().get(0));
        Assert.assertEquals("addReview", request.url().pathSegments().get(1));
        Assert.assertEquals(4, request.url().queryParameterNames().size());
        Assert.assertEquals("com.cpuid.cpu_z", request.url().queryParameter("doc"));
        Assert.assertEquals("Работает!", request.url().queryParameter("content"));
        Assert.assertEquals("", request.url().queryParameter("title"));
        Assert.assertEquals("5", request.url().queryParameter("rating"));
    }

    @Test
    public void recommendations() throws Exception {
        ListResponse response = api.recommendations("com.cpuid.cpu_z", GooglePlayAPI.RECOMMENDATION_TYPE.ALSO_VIEWED, 0, 20);

        Assert.assertTrue(response.getDocCount() > 0);
        Assert.assertEquals(20, response.getDoc(0).getChildCount());
        Assert.assertEquals("com.abs.cpu_z_advance", response.getDoc(0).getChild(0).getDetails().getAppDetails().getPackageName());

        List<Request> requests = api.getRequests();
        Assert.assertEquals(1, requests.size());
        Request request = requests.get(0);
        Assert.assertEquals(2, request.url().pathSegments().size());
        Assert.assertEquals("fdfe", request.url().pathSegments().get(0));
        Assert.assertEquals("rec", request.url().pathSegments().get(1));
        Assert.assertEquals(5, request.url().queryParameterNames().size());
        Assert.assertEquals("com.cpuid.cpu_z", request.url().queryParameter("doc"));
        Assert.assertEquals("1", request.url().queryParameter("rt"));
        Assert.assertEquals("3", request.url().queryParameter("c"));
        Assert.assertEquals("0", request.url().queryParameter("o"));
        Assert.assertEquals("20", request.url().queryParameter("n"));
    }

    private MockGooglePlayAPI initApi() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getSystemResourceAsStream("device-honami.properties"));
        } catch (IOException e) {
            System.out.println("device-honami.properties not found");
            return null;
        }

        PropertiesDeviceInfoProvider deviceInfoProvider = new PropertiesDeviceInfoProvider();
        deviceInfoProvider.setProperties(properties);
        deviceInfoProvider.setLocaleString(Locale.ENGLISH.toString());
        deviceInfoProvider.setTimeToReport(1482626488L);

        MockGooglePlayAPI api = new MockGooglePlayAPI(EMAIL);
        api.setLocale(Locale.US);
        api.setDeviceInfoProvider(deviceInfoProvider);
        api.setGsfId(GSFID);
        api.setToken(TOKEN);
        return api;
    }

}

class MockGooglePlayAPI extends GooglePlayAPI {

    OkHttpClientWrapper getClient() {
        if (this.client == null) {
            this.client = new MockOkHttpClientWrapper();
        }
        return this.client;
    }

    List<Request> getRequests() {
        return ((MockOkHttpClientWrapper) this.getClient()).getRequests();
    }

    MockGooglePlayAPI(String email) {
        super(email);
    }
}

class MockOkHttpClientWrapper extends OkHttpClientWrapper {
	
	private List<Request> requests = new ArrayList<>();

    @Override
    byte[] request(Request.Builder requestBuilder, Map<String, String> headers) throws IOException {
        byte[] body = null;
		Request request = requestBuilder.headers(Headers.of(headers)).build();
		this.requests.add(request);
        String fileName = getBodyFileName(request);
        System.out.println("Checking if " + fileName + " exists");
        URL url = getClass().getClassLoader().getResource(fileName);
        if (null != url) {
            String path = java.net.URLDecoder.decode(new File(url.getFile()).getAbsolutePath(), "UTF-8");
            System.out.println("Body FOUND. Reading from disk. " + path);
            body = read(path);
        } else {
            System.out.println("Body NOT found. Making a live request.");
            try {
                body = super.request(requestBuilder, headers);
            } catch (GooglePlayException e) {
                System.out.println("Error making a live request: " + e.getMessage());
            } finally {
                write(fileName, body);
            }
        }
        return body;
    }
	
	List<Request> getRequests() {
		return this.requests;
	}

    private byte[] read(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            System.out.println("Could not read from " + path + ": " + e.getMessage());
        }
        return null;
    }

    static byte[] getBodyBytes(Request request) {
        if (request.body() == null) {
            return null;
        }
        Request copy = request.newBuilder().build();
        Buffer buffer = new Buffer();
        try {
            copy.body().writeTo(buffer);
            return buffer.readByteArray();
        } catch (IOException e) {
            System.out.println("Could not read body");
        }
        return null;
    }

    static Map<String, String> parseQueryString(byte[] query) {
        Map<String, String> vars = new HashMap<>();
        String[] pairs = new String(query).split("\\&");
        for (String pair: pairs) {
            String[] fields = pair.split("=");
            if (fields.length > 0) {
                try {
                    vars.put(URLDecoder.decode(fields[0], "UTF-8"), fields.length == 1 ? null : URLDecoder.decode(fields[1], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    // Unlikely
                }
            }
        }
        return vars;
    }

    private static void write(String path, byte[] body) {
        FileOutputStream stream;
        try {
            stream = new FileOutputStream(path);
            stream.write(body);
            stream.close();
        } catch (IOException e) {
            System.out.println("Could not write to " + path + ": " + e.getMessage());
        }
    }

    private static String getBodyFileName(Request request) {
        StringBuilder fileName = new StringBuilder();
        fileName.append("request");
        fileName.append(request.url().encodedPath().replace("/", "."));
        for (String key: request.url().queryParameterNames()) {
            fileName.append(".").append(key).append(".").append(request.url().queryParameter(key));
        }
        byte[] body = getBodyBytes(request);
        if (null != body) {
            fileName.append(".").append(Arrays.hashCode(body));
        }
        fileName.append(".bin");
        return fileName.toString();
    }
}