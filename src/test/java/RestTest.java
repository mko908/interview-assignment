import com.benrevo.inventory.AppConfig;
import com.benrevo.inventory.DataAccess;
import com.benrevo.inventory.InventoryManagementApplication;
import com.benrevo.inventory.RestController;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Base64;

/**
 * @author John Hurst
 */
@RunWith(SpringRunner.class)
@WebMvcTest(RestController.class)
@ContextConfiguration(classes = {AppConfig.class})
public class RestTest {

    @Autowired
    private MockMvc mockMvc;

    private static JSONObject registerTestObj = new JSONObject()
            .put("username", "test")
            .put("password", "test");

    private static JSONObject authTestObj = new JSONObject()
            .put("username", "login")
            .put("password", "login");

    private static JSONObject addTestObj = new JSONObject()
            .put("username", "login")
            .put("name", "apples")
            .put("amount", 4);

    private static JSONObject updateTestObj = new JSONObject()
            .put("username", "login")
            .put("data", new JSONArray()
                .put(new JSONObject()
                    .put("id", 1)
                    .put("name", "bananas")
                    .put("amount", 5)));

    private static JSONObject deleteTestObj = new JSONObject()
            .put("username", "login")
            .put("id", 1);

    private static DataAccess dataAccess = new DataAccess();

    private static String authHash;

    @BeforeClass
    public static void before() throws Exception {
        dataAccess.addUser(authTestObj.getString("username"), authTestObj.getString("password"));
        authHash = Base64.getEncoder().encodeToString(DigestUtils.getSha512Digest()
                .digest((authTestObj.getString("username") + authTestObj.getString("password")).getBytes()));
    }

    @AfterClass
    public static void after() throws Exception {
        dataAccess.cleanDatabase();
    }

    @After
    public void afterTest() throws Exception {
        dataAccess.cleanInventory();
    }

    @Test
    public void test() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/register")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerTestObj.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString());
        assert response.has("auth");
        assert response.get("auth") instanceof String;
    }

    @Test
    public void addInventory() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/inventory/add")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authentication", authHash)
                .contentType(MediaType.APPLICATION_JSON)
                .content(addTestObj.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString());
        assert response.has("id");
        assert response.get("id").equals(1);

        JSONArray expectedInventory = new JSONArray()
                .put(new JSONObject()
                    .put("id", 1)
                    .put("name", "apples")
                    .put("amount", 4));
        assert dataAccess.getInventory(authTestObj.getString("username")).toString().equals(expectedInventory.toString());
    }

    @Test
    public void updateInventory() throws Exception {
        dataAccess.addItem(addTestObj.getString("name"), addTestObj.getInt("amount"), authTestObj.getString("username"));

        this.mockMvc.perform(MockMvcRequestBuilders.put("/inventory/update")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authentication", authHash)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateTestObj.toString()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        JSONArray expectedInventory = new JSONArray()
                .put(new JSONObject()
                    .put("id", 1)
                    .put("name", "bananas")
                    .put("amount", 5));
        assert dataAccess.getInventory(authTestObj.getString("username")).toString().equals(expectedInventory.toString());
    }

    @Test
    public void login() throws Exception {

        dataAccess.addItem(addTestObj.getString("name"), addTestObj.getInt("amount"), authTestObj.getString("username"));

        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(authTestObj.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString());
        JSONArray expectedData = new JSONArray()
                .put(new JSONObject()
                    .put("name", "apples")
                    .put("amount", 4)
                    .put("id", 1));
        assert response.has("auth");
        assert response.get("auth").equals(authHash);
        assert response.has("data");
        assert response.get("data").toString().equals(expectedData.toString());
    }

    @Test
    public void deleteInventory() throws Exception {

        dataAccess.addItem(addTestObj.getString("name"), addTestObj.getInt("amount"), authTestObj.getString("username"));

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/inventory/delete")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authentication", authHash)
                .contentType(MediaType.APPLICATION_JSON)
                .content(deleteTestObj.toString()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        JSONArray expectedInventory = new JSONArray();
        System.err.println(expectedInventory.toString());
        System.err.println(dataAccess.getInventory(authTestObj.getString("username")).toString());
        assert dataAccess.getInventory(authTestObj.getString("username")).toString().equals(expectedInventory.toString());
    }
}
