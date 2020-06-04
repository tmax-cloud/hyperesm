package com.tmax.hypercloud.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.kubernetes.client.proto.V1Authentication.TokenReview;
import io.kubernetes.client.proto.V1Authentication.TokenReviewStatus;
import io.kubernetes.client.proto.V1Authentication.UserInfo;

public class AuthenticationWebhook {
	public static TokenReview reviewToken(TokenReview review) {
		DecodedJWT jwt = JWT.decode(review.getSpec().getToken());
		return TokenReview.newBuilder(review).clearSpec().setStatus(
				TokenReviewStatus.newBuilder().setAuthenticated(true)
				.setUser(UserInfo.newBuilder().setUsername(jwt.getClaim("kubernetes.io/serviceaccount/service-account.name").asString())
						.setUid(jwt.getClaim("kubernetes.io/serviceaccount/service-account.uid").asString()))).build();
	}
}
