/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.dts.datasource.executor.event;

import com.google.common.base.Optional;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;


/**
 * SQL execution event.
 *
 * @author zhangliang
 */
public abstract class AbstractExecutionEvent {
    
    private final String id;
    
    private final String dataSource;
    
    private final String sql;
    
    private final List<Object> parameters;
    
    private EventExecutionType eventExecutionType;
    
    private Optional<SQLException> exception;
    
    public AbstractExecutionEvent(final String dataSource, final String sql, final List<Object> parameters) {
        id = UUID.randomUUID().toString();
        this.dataSource = dataSource;
        this.sql = sql;
        this.parameters = parameters;
        eventExecutionType = EventExecutionType.BEFORE_EXECUTE;
    }

    public String getId() {
        return id;
    }

    public String getDataSource() {
        return dataSource;
    }

    public String getSql() {
        return sql;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public EventExecutionType getEventExecutionType() {
        return eventExecutionType;
    }

    public void setEventExecutionType(final EventExecutionType eventExecutionType) {
        this.eventExecutionType = eventExecutionType;
    }

    public Optional<SQLException> getException() {
        return exception;
    }

    public void setException(final Optional<SQLException> exception) {
        this.exception = exception;
    }
}
