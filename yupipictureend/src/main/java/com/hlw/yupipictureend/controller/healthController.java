package com.hlw.yupipictureend.controller;

import com.hlw.yupipictureend.common.BaseResponse;
import com.hlw.yupipictureend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class healthController {
    @GetMapping("/health")
    @ResponseBody
    public String healthCheck() {

        return "[\n" +
                "    {\n" +
                "        \"name\": \"Cafe Deadend\",\n" +
                "        \"type\": \"Coffee & Tea Shop\",\n" +
                "        \"location\": \"Hong Kong\",\n" +
                "        \"imageName\": \"cafe1\",\n" +
                "        \"quantity\": 0,\n" +
                "        \"price\": 45.32\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\": \"Homei\",\n" +
                "        \"type\": \"Coffee & Tea Shop\",\n" +
                "        \"location\": \"Hong Kong\",\n" +
                "        \"imageName\": \"cafe2\",\n" +
                "        \"quantity\": 0,\n" +
                "        \"price\": 78.91\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\": \"Teakha\",\n" +
                "        \"type\": \"Coffee & Tea Shop\",\n" +
                "        \"location\": \"Hong Kong\",\n" +
                "        \"imageName\": \"cafe3\",\n" +
                "        \"quantity\": 0,\n" +
                "        \"price\": 22.15\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\": \"Cafe Lois!\",\n" +
                "        \"type\": \"Coffee & Tea Shop\",\n" +
                "        \"location\": \"Hong Kong\",\n" +
                "        \"imageName\": \"cafe4\",\n" +
                "        \"quantity\": 0,\n" +
                "        \"price\": 66.47\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\": \"Petite Oyster\",\n" +
                "        \"type\": \"Coffee & Tea Shop\",\n" +
                "        \"location\": \"Hong Kong\",\n" +
                "        \"imageName\": \"cafe5\",\n" +
                "        \"quantity\": 0,\n" +
                "        \"price\": 12.78\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\": \"For Kee Restaurant\",\n" +
                "        \"type\": \"Coffee & Tea Shop\",\n" +
                "        \"location\": \"Hong Kong\",\n" +
                "        \"imageName\": \"cafe6\",\n" +
                "        \"quantity\": 0,\n" +
                "        \"price\": 91.03\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\": \"The Coffee Roastery\",\n" +
                "        \"type\": \"Coffee & Tea Shop\",\n" +
                "        \"location\": \"London\",\n" +
                "        \"imageName\": \"cafe7\",\n" +
                "        \"quantity\": 0,\n" +
                "        \"price\": 55.55\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\": \"Chai Haven\",\n" +
                "        \"type\": \"Coffee & Tea Shop\",\n" +
                "        \"location\": \"New Delhi\",\n" +
                "        \"imageName\": \"cafe8\",\n" +
                "        \"quantity\": 0,\n" +
                "        \"price\": 33.66\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\": \"Brew & Blend\",\n" +
                "        \"type\": \"Coffee & Tea Shop\",\n" +
                "        \"location\": \"Sydney\",\n" +
                "        \"imageName\": \"cafe9\",\n" +
                "        \"quantity\": 0,\n" +
                "        \"price\": 87.23\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\": \"Tea Time Retreat\",\n" +
                "        \"type\": \"Coffee & Tea Shop\",\n" +
                "        \"location\": \"Tokyo\",\n" +
                "        \"imageName\": \"cafe10\",\n" +
                "        \"quantity\": 0,\n" +
                "        \"price\": 19.89\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\": \"Java Junction\",\n" +
                "        \"type\": \"Coffee & Tea Shop\",\n" +
                "        \"location\": \"New York\",\n" +
                "        \"imageName\": \"cafe11\",\n" +
                "        \"quantity\": 0,\n" +
                "        \"price\": 70.11\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\": \"Leaf & Bean\",\n" +
                "        \"type\": \"Coffee & Tea Shop\",\n" +
                "        \"location\": \"Paris\",\n" +
                "        \"imageName\": \"cafe12\",\n" +
                "        \"quantity\": 0,\n" +
                "        \"price\": 40.40\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\": \"Espresso Emporium\",\n" +
                "        \"type\": \"Coffee & Tea Shop\",\n" +
                "        \"location\": \"Milan\",\n" +
                "        \"imageName\": \"cafe13\",\n" +
                "        \"quantity\": 0,\n" +
                "        \"price\": 61.90\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\": \"Matcha Magic\",\n" +
                "        \"type\": \"Coffee & Tea Shop\",\n" +
                "        \"location\": \"Seoul\",\n" +
                "        \"imageName\": \"cafe14\",\n" +
                "        \"quantity\": 0,\n" +
                "        \"price\": 27.72\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\": \"Cuppa Corner\",\n" +
                "        \"type\": \"Coffee & Tea Shop\",\n" +
                "        \"location\": \"Toronto\",\n" +
                "        \"imageName\": \"cafe15\",\n" +
                "        \"quantity\": 0,\n" +
                "        \"price\": 53.85\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\": \"Bean & Leaf Lounge\",\n" +
                "        \"type\": \"Coffee & Tea Shop\",\n" +
                "        \"location\": \"Cape Town\",\n" +
                "        \"imageName\": \"cafe16\",\n" +
                "        \"quantity\": 0,\n" +
                "        \"price\": 39.21\n" +
                "    }\n" +
                "]";
    }
}
