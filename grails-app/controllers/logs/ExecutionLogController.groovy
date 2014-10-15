package logs

import org.springframework.dao.DataIntegrityViolationException

class ExecutionLogController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [executionLogInstanceList: ExecutionLog.list(params), executionLogInstanceTotal: ExecutionLog.count()]
    }

    def create() {
        [executionLogInstance: new ExecutionLog(params)]
    }

    def save() {
        def executionLogInstance = new ExecutionLog(params)
        if (!executionLogInstance.save(flush: true)) {
            render(view: "create", model: [executionLogInstance: executionLogInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'executionLog.label', default: 'ExecutionLog'), executionLogInstance.id])
        redirect(action: "show", id: executionLogInstance.id)
    }

    def show(Long id) {
        def executionLogInstance = ExecutionLog.get(id)
        if (!executionLogInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'executionLog.label', default: 'ExecutionLog'), id])
            redirect(action: "list")
            return
        }

        [executionLogInstance: executionLogInstance]
    }

    def edit(Long id) {
        def executionLogInstance = ExecutionLog.get(id)
        if (!executionLogInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'executionLog.label', default: 'ExecutionLog'), id])
            redirect(action: "list")
            return
        }

        [executionLogInstance: executionLogInstance]
    }

    def update(Long id, Long version) {
        def executionLogInstance = ExecutionLog.get(id)
        if (!executionLogInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'executionLog.label', default: 'ExecutionLog'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (executionLogInstance.version > version) {
                executionLogInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'executionLog.label', default: 'ExecutionLog')] as Object[],
                          "Another user has updated this ExecutionLog while you were editing")
                render(view: "edit", model: [executionLogInstance: executionLogInstance])
                return
            }
        }

        executionLogInstance.properties = params

        if (!executionLogInstance.save(flush: true)) {
            render(view: "edit", model: [executionLogInstance: executionLogInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'executionLog.label', default: 'ExecutionLog'), executionLogInstance.id])
        redirect(action: "show", id: executionLogInstance.id)
    }

    def delete(Long id) {
        def executionLogInstance = ExecutionLog.get(id)
        if (!executionLogInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'executionLog.label', default: 'ExecutionLog'), id])
            redirect(action: "list")
            return
        }

        try {
            executionLogInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'executionLog.label', default: 'ExecutionLog'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'executionLog.label', default: 'ExecutionLog'), id])
            redirect(action: "show", id: id)
        }
    }
}
