/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.lite.console.repository.impl;

import org.apache.shardingsphere.elasticjob.lite.console.exception.JobConsoleException;
import org.apache.shardingsphere.elasticjob.lite.console.repository.XmlRepository;
import org.apache.shardingsphere.elasticjob.lite.console.util.HomeFolderUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;

/**
 * Abstract XML repository implementation.
 *
 * @param <E> type of data
 */
public abstract class AbstractXmlRepositoryImpl<E> implements XmlRepository<E> {
    
    private final File file;
    
    private final Class<E> clazz;
    
    private JAXBContext jaxbContext;
    
    protected AbstractXmlRepositoryImpl(final String fileName, final Class<E> clazz) {
        file = new File(HomeFolderUtils.getFilePathInHomeFolder(fileName));
        this.clazz = clazz;
        HomeFolderUtils.createHomeFolderIfNotExisted();
        try {
            jaxbContext = JAXBContext.newInstance(clazz);
        } catch (final JAXBException ex) {
            throw new JobConsoleException(ex);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public synchronized E load() {
        if (!file.exists()) {
            try {
                return clazz.newInstance();
            } catch (final InstantiationException | IllegalAccessException ex) {
                throw new JobConsoleException(ex);
            }
        }
        try {
            return (E) jaxbContext.createUnmarshaller().unmarshal(file);
        } catch (final JAXBException ex) {
            throw new JobConsoleException(ex);
        }
    }
    
    @Override
    public synchronized void save(final E entity) {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(entity, file);
        } catch (final JAXBException ex) {
            throw new JobConsoleException(ex);
        }
    }
}
