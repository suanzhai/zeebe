package org.camunda.optimize.rest;

import org.camunda.optimize.dto.optimize.query.sharing.SharingDto;
import org.camunda.optimize.test.it.rule.ElasticSearchIntegrationTestRule;
import org.camunda.optimize.test.it.rule.EmbeddedOptimizeRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Askar Akhmerov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/it/it-applicationContext.xml"})
public class SharingRestServiceIT {

  public static final String BEARER = "Bearer ";
  public static final String SHARE = "share";
  public ElasticSearchIntegrationTestRule elasticSearchRule = new ElasticSearchIntegrationTestRule();
  public EmbeddedOptimizeRule embeddedOptimizeRule = new EmbeddedOptimizeRule();
  @Rule
  public RuleChain chain = RuleChain
      .outerRule(elasticSearchRule).around(embeddedOptimizeRule);

  @Test
  public void createNewShareWithoutAuthentication() {
    // when
    Response response =
        embeddedOptimizeRule.target(SHARE)
            .request()
            .post(Entity.json(""));

    // then the status code is not authorized
    assertThat(response.getStatus(), is(401));
  }

  @Test
  public void createNewShare() {
    //given
    String token = embeddedOptimizeRule.getAuthenticationToken();

    // when
    Response response =
        embeddedOptimizeRule.target(SHARE)
            .request()
            .header(HttpHeaders.AUTHORIZATION, BEARER + token)
            .post(Entity.json(createShare()));

    // then the status code is okay
    assertThat(response.getStatus(), is(200));
    String id =
        response.readEntity(String.class);
    assertThat(id, is(notNullValue()));
  }

  @Test
  public void shareIsNotCreatedForSameResourceTwice() {
    //given
    String token = embeddedOptimizeRule.getAuthenticationToken();

    // when
    SharingDto share = createShare();
    Response response =
        embeddedOptimizeRule.target(SHARE)
            .request()
            .header(HttpHeaders.AUTHORIZATION, BEARER + token)
            .post(Entity.json(share));

    // then the status code is okay
    assertThat(response.getStatus(), is(200));
    String id =
        response.readEntity(String.class);
    assertThat(id, is(notNullValue()));

    response =
        embeddedOptimizeRule.target(SHARE)
            .request()
            .header(HttpHeaders.AUTHORIZATION, BEARER + token)
            .post(Entity.json(share));

    assertThat(id, is(response.readEntity(String.class)));
  }

  private SharingDto createShare() {
    SharingDto sharingDto = new SharingDto();
    sharingDto.setResourceId("fake");
    return sharingDto;
  }


}
