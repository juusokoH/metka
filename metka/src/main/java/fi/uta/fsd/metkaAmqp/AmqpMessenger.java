/**************************************************************************************
 * Copyright (c) 2013-2015, Finnish Social Science Data Archive/University of Tampere *
 *                                                                                    *
 * All rights reserved.                                                               *
 *                                                                                    *
 * Redistribution and use in source and binary forms, with or without modification,   *
 * are permitted provided that the following conditions are met:                      *
 * 1. Redistributions of source code must retain the above copyright notice, this     *
 *    list of conditions and the following disclaimer.                                *
 * 2. Redistributions in binary form must reproduce the above copyright notice,       *
 *    this list of conditions and the following disclaimer in the documentation       *
 *    and/or other materials provided with the distribution.                          *
 * 3. Neither the name of the copyright holder nor the names of its contributors      *
 *    may be used to endorse or promote products derived from this software           *
 *    without specific prior written permission.                                      *
 *                                                                                    *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND    *
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED      *
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE             *
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR   *
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES     *
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;       *
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON     *
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT            *
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                       *
 **************************************************************************************/

package fi.uta.fsd.metkaAmqp;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import fi.uta.fsd.Logger;

import java.io.IOException;

class AmqpMessenger {

        private ConnectionFactory factory = null;
        private Connection connection = null;
        private Channel channel = null;
        private AMQPState state = AMQPState.AMQP_STOPPED;

        void init(String host, int port, String user, String password) {
            try {
                if (factory == null) {
                    factory = new ConnectionFactory();
                }
                factory.setHost(host);
                factory.setUsername(user);
                factory.setPassword(password);
                factory.setPort(port);
                connection = factory.newConnection();

                if (!connection.isOpen()) {
                    connection = factory.newConnection();
                }
                channel = connection.createChannel();
                state = AMQPState.AMQP_READY;
            } catch(IOException ioe) {
                Logger.error(AmqpMessenger.class, "AMQP channel creation failed.", ioe);
                channel = null;
                state = AMQPState.AMQP_CONNECTION_FAILED;
            }
        }

        void clean() {
            try {
                if (channel != null && channel.isOpen()) {
                    channel.close();
                }
                if (connection != null && connection.isOpen()) {
                    connection.close();
                }
                state = AMQPState.AMQP_STOPPED;
            } catch(IOException ioe) {
                Logger.error(getClass(), "IOException during AMQP cleanup.", ioe);
            }
        }

        void logState() {
            Logger.debug(getClass(), "AMQP Messenger is currently at state: "+state);
        }

        void write(String exchange, String routingKey, String message) {
            if(state == AMQPState.AMQP_READY) {
                try {
                    // TODO: Move routing key to message
                    channel.basicPublish(exchange, routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
                } catch(IOException ioe) {
                    Logger.error(getClass(), "AMQP message write failed.", ioe);
                    state = AMQPState.AMQP_CONNECTION_FAILED;
                    logState();
                }
            } else {
                Logger.error(getClass(), "AMQP messenger is not in READY state, instead being in state: "+state);
            }
        }

        private static enum AMQPState {
            AMQP_READY,
            AMQP_CONNECTION_FAILED,
            AMQP_STOPPED
        }
    }