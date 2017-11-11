package com.github.enviableyapper0;

import com.github.enviableyapper0.beans.Order;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.net.URI;

public class OrderDashboardDAO {
    private Client client;
    private WebTarget target;

    public OrderDashboardDAO(URI baseURI) {
        this.client = ClientBuilder.newClient();
        this.target = client.target(baseURI);
    }

    public Order[] getAllOrder() {
        return target.path("order").
                request().
                accept(MediaType.APPLICATION_JSON).
                get(Order[].class);
    }

    public void deleteOrder(int orderId) {
        target.path("order").path(Integer.toString(orderId)).request().delete();
    }

    public void deleteIndividualFoodItem(int orderId, int foodIndex) {
        target.path("order").path(Integer.toString(orderId)).path(Integer.toString(foodIndex)).request().delete();
    }
}
