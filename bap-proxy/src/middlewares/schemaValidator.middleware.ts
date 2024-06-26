import { NextFunction, Request, Response } from "express";
import * as OpenApiValidator from "express-openapi-validator";
import { Exception, ExceptionType } from "../models/exception.model";
import { Locals } from "../interfaces/locals.interface";
import { getConfig } from "../utils/config.utils";
import fs from "fs";
import path from "path";
import logger from "../utils/logger.utils";
const protocolServerLevel = `${getConfig().app.mode.toUpperCase()}-${getConfig().app.gateway.mode.toUpperCase()}`;

export const schemaErrorHandler = (
  err: any,
  req: Request,
  res: Response,
  next: NextFunction
) => {
  if (err instanceof Exception) {
    next(err);
  } else {
    const errorData = new Exception(
      ExceptionType.OpenApiSchema_ParsingError,
      `OpenApiValidator Error at ${protocolServerLevel}`,
      err.status,
      err
    );

    next(errorData);
  }
};

export const openApiValidatorMiddleware = async (
  req: Request,
  res: Response<{}, Locals>,
  next: NextFunction
) => {
  // const version = req?.body?.context?.core_version
  //   ? req?.body?.context?.core_version
  //   : req?.body?.context?.version;
  const version = "1.1.0"
  logger.info("#######req is ", JSON.stringify(req?.body))
  logger.info("############### version is ", version)
  let specFile: string;
  let isDomainSpecificExist = false;
  if (getConfig().app.useDomainSpecificYAML) {
    try {
      isDomainSpecificExist = (
        await fs.promises.readdir(
          `${path.join(path.resolve(__dirname, "../../"))}/schemas`
        )
      ).includes(`${req?.body?.context?.domain}_${version}.yaml`);
    } catch (error) {
      isDomainSpecificExist = false;
    }
  }
  specFile = getConfig().app.useDomainSpecificYAML
    ? isDomainSpecificExist
      ? `schemas/${req?.body?.context?.domain}_${version}.yaml`
      : `schemas/core_${version}.yaml`
    : `schemas/core_${version}.yaml`;

  const openApiValidator = OpenApiValidator.middleware({
    apiSpec: specFile,
    validateRequests: true,
    validateResponses: false,
    $refParser: {
      mode: "dereference"
    }
  });

  const walkSubstack = function (
    stack: any,
    req: any,
    res: any,
    next: NextFunction
  ) {
    if (typeof stack === "function") {
      stack = [stack];
    }
    const walkStack = function (i: any, err?: any) {
      if (err) {
        return schemaErrorHandler(err, req, res, next);
      }
      if (i >= stack.length) {
        return next();
      }
      stack[i](req, res, walkStack.bind(null, i + 1));
    };
    walkStack(0);
  };
  walkSubstack([...openApiValidator], req, res, next);
};
