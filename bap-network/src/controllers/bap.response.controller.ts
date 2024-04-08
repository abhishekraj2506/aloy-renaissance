import { NextFunction, Request, Response } from "express";
import { Locals } from "../interfaces/locals.interface";
import { Exception, ExceptionType } from "../models/exception.model";
import { BecknErrorType } from "../schemas/becknError.schema";
import { ResponseActions } from "../schemas/configs/actions.app.config.schema";
import {
  acknowledgeACK,
  acknowledgeNACK,
} from "../utils/acknowledgement.utils";
import { ActionUtils } from "../utils/actions.utils";
import { RequestCache } from "../utils/cache/request.cache.utils";
import { getConfig } from "../utils/config.utils";
import { GatewayUtils } from "../utils/gateway.utils";
import logger from "../utils/logger.utils";

export const clientProtocolApiRequestHandler = async (
  req: Request,
  res: Response<{}, Locals>,
  next: NextFunction,
  action: ResponseActions
) => {
  try {
    const requestAction = ActionUtils.getCorrespondingRequestAction(action);
    const message_id = req.body.context.message_id;

    const requestCache = await RequestCache.getInstance().check(
      message_id,
      requestAction
    );
    if (!requestCache) {
      acknowledgeNACK(res, req.body.context, {
        // TODO: change the error code.
        code: 6781616,
        message: `Response timed out for ${message_id} and action:${requestAction}, as requestCache not found`,
        type: BecknErrorType.coreError,
      });
      return;
    }

    logger.info(
      `\nSending ACK to BPP for Context: ${JSON.stringify(
        req.body.context
      )}\n\n`
    );
    acknowledgeACK(res, req.body.context);

    logger.info(`Sending response from BPP to inbox queue`);
    logger.info(`response: ${JSON.stringify(req.body)}`);

    await GatewayUtils.getInstance().sendToClientSideGateway(req.body);
  } catch (err) {
    let exception: Exception | null = null;
    if (err instanceof Exception) {
      exception = err;
    } else {
      exception = new Exception(
        ExceptionType.Response_Failed,
        `BAP Response Failed at clientProtocolApiRequestHandler at ${
          getConfig().app.mode
        } ${getConfig().app.gateway.mode}`,
        500,
        err
      );
    }

    logger.error(exception);
  }
};