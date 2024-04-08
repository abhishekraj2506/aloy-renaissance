import { NextFunction, Request, Response, Router } from "express";
import { clientProtocolApiRequestHandler } from "../controllers/bap.response.controller";
import { unConfigureActionHandler } from "../controllers/unconfigured.controller";
import {
  authValidatorMiddleware
} from "../middlewares/auth.middleware";
import { jsonCompressorMiddleware } from "../middlewares/jsonParser.middleware";
import {
  openApiValidatorMiddleware
} from "../middlewares/schemaValidator.middleware";
import { ResponseActions } from "../schemas/configs/actions.app.config.schema";
import { AppMode } from "../schemas/configs/app.config.schema";
import { GatewayMode } from "../schemas/configs/gateway.app.config.schema";
import { getConfig } from "../utils/config.utils";
import logger from "../utils/logger.utils";

export const responsesRouter = Router();

// BAP Network-Side Gateway Configuration.
if (
  getConfig().app.mode === AppMode.bap &&
  getConfig().app.gateway.mode === GatewayMode.network
) {
  const responseActions = getConfig().app.actions.responses;
  Object.keys(ResponseActions).forEach((action) => {
    if (responseActions[action as ResponseActions]) {
      responsesRouter.post(
        `/${action}`,
        jsonCompressorMiddleware,
        authValidatorMiddleware,
        openApiValidatorMiddleware,
        async (req: Request, res: Response, next: NextFunction) => {
          logger.info(`response from bpp: ${JSON.stringify(req.body)}`);
          await clientProtocolApiRequestHandler(
            req,
            res,
            next,
            action as ResponseActions
          );
        }
      );
    } else {
      responsesRouter.post(
        `/${action}`,
        async (req: Request, res: Response, next: NextFunction) => {
          await unConfigureActionHandler(req, res, next, action);
        }
      );
    }
  });
}