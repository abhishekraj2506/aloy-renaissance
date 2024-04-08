import { Exception, ExceptionType } from "../models/exception.model";
import { GatewayMode } from "../schemas/configs/gateway.app.config.schema";
import { getConfig } from "./config.utils";
import { MQClient } from "./rbtmq.utils";

export class GatewayUtils {
    public static getInstance() {
        if (!GatewayUtils.instance) {
            GatewayUtils.instance = new GatewayUtils();
        }
        return GatewayUtils.instance;
    }

    private static instance: GatewayUtils;

    private mqClient: MQClient;

    private constructor() {
        this.mqClient = new MQClient(getConfig().app.gateway.amqpURL);
    }

    public async initialize() {
        await this.mqClient.connect();
        // await this.mqClient.assertQueue(getConfig().app.gateway.inboxQueue); 
        // switch(getConfig().app.gateway.mode){
        //     case GatewayMode.client:{
        //         switch (getConfig().app.mode) {
        //             case AppMode.bap:{
        //                 await this.mqClient.consumeMessage(getConfig().app.gateway.inboxQueue, clientProtocolResponseConsumer);
        //                 break;
        //             }
        //         }
        //         break;
        //     }
        // }
    }

    public async sendToNetworkSideGateway(data: any) {
        if (getConfig().app.gateway.mode === GatewayMode.network) {
            throw new Exception(ExceptionType.Gateway_InvalidUse, "Gateway is in network mode, cannot send data to network side gateway", 500);
        }

        await this.mqClient.publishMessage(getConfig().app.gateway.outboxQueue, data);
    }
}