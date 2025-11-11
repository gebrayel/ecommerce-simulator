package com.tuempresa.ecommerce.orders.domain.port.out;

import com.tuempresa.ecommerce.orders.domain.model.ServiceLog;

public interface ServiceLogRepositoryPort {

    ServiceLog save(ServiceLog serviceLog);
}


