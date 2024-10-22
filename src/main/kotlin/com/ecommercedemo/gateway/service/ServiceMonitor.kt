package com.ecommercedemo.gateway.service

import com.ecommercedemo.common.redis.RedisService
import com.netflix.appinfo.InstanceInfo
import com.netflix.discovery.EurekaClient
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ServiceMonitor(
    private val eurekaClient: EurekaClient,
    private val redisService: RedisService
) {
    @Scheduled(fixedRate = 30000)
    fun checkForOfflineServices() {
        val applications = eurekaClient.applications
        applications.registeredApplications.forEach { app ->
            app.instances.forEach { instance ->
                if (instance.status == InstanceInfo.InstanceStatus.DOWN) {
                    handleOfflineService(instance.appName)
                }
            }
        }
    }

    private fun handleOfflineService(serviceName: String) {
        println("Service $serviceName is offline. Removing this producer from kafka-topic-registry.")
        redisService.deregisterProducer(serviceName)
    }
}