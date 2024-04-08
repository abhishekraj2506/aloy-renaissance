import { NextFunction, Request, Response } from "express";
import { RequestActions } from "../schemas/configs/actions.app.config.schema";
import logger from "../utils/logger.utils";
import * as AmqbLib from "amqplib";
import { Exception, ExceptionType } from "../models/exception.model";
import { acknowledgeACK } from "../utils/acknowledgement.utils";
import { GatewayUtils } from "../utils/gateway.utils";
import { ActionUtils } from "../utils/actions.utils";
import { RequestCache } from "../utils/cache/request.cache.utils";
import { parseRequestCache } from "../schemas/cache/request.cache.schema";
import { getSubscriberDetails } from "../utils/lookup.utils";
import { Locals } from "../interfaces/locals.interface";
import moment from "moment";
import { getConfig } from "../utils/config.utils";
import { ClientConfigType } from "../schemas/configs/client.config.schema";
import { requestCallback } from "../utils/callback.utils";
import { telemetryCache } from "../schemas/cache/telemetry.cache";
import {
  createTelemetryEvent,
  processTelemetry
} from "../utils/telemetry.utils";

const doHandleRequest = async (
  req: Request,
  res: Response<{}, Locals>,
  next: NextFunction,
  action: RequestActions) => {
  try {
    logger.info("$$$ in bppNetworkRequestHandler continue after ack")
    const message_id = req.body.context.message_id;
    const transaction_id = req.body.context.transaction_id;
    const ttl = moment.duration(req.body.context.ttl).asMilliseconds();

    await RequestCache.getInstance().cache(
      parseRequestCache(transaction_id, message_id, action, res.locals.sender!),
      ttl
    );
    
    await GatewayUtils.getInstance().sendToClientSideGateway(req.body);

  } catch (err) {
    let exception: Exception | null = null;
    if (err instanceof Exception) {
      exception = err;
    } else {
      exception = new Exception(
        ExceptionType.Request_Failed,
        "BPP Request Failed at bppNetworkRequestHandler",
        500,
        err
      );
    }

    logger.error(exception);
  }
}

export const sellerProtocolApiRequestHandler = async (
  req: Request,
  res: Response<{}, Locals>,
  next: NextFunction,
  action: RequestActions
) => {
  logger.info("in sellerProtocolApiRequestHandler")
  try {
    doHandleRequest(req, res, next, action);
    acknowledgeACK(res, req.body.context);
    return
  } catch (err) {
    let exception: Exception | null = null;
    if (err instanceof Exception) {
      exception = err;
    } else {
      exception = new Exception(
        ExceptionType.Request_Failed,
        "BPP Request Failed at bppNetworkRequestHandler",
        500,
        err
      );
    }

    logger.error(exception);
  }
};