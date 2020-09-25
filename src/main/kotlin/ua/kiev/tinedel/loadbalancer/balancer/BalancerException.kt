package ua.kiev.tinedel.loadbalancer.balancer

/**
 * A balancer exception
 *
 * Class represents errors during balancing
 *
 * @param message what went wrong
 */
class BalancerException(message: String) : RuntimeException(message)
