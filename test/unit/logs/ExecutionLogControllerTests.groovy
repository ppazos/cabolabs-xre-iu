package logs



import org.junit.*
import grails.test.mixin.*

@TestFor(ExecutionLogController)
@Mock(ExecutionLog)
class ExecutionLogControllerTests {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void testIndex() {
        controller.index()
        assert "/executionLog/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.executionLogInstanceList.size() == 0
        assert model.executionLogInstanceTotal == 0
    }

    void testCreate() {
        def model = controller.create()

        assert model.executionLogInstance != null
    }

    void testSave() {
        controller.save()

        assert model.executionLogInstance != null
        assert view == '/executionLog/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/executionLog/show/1'
        assert controller.flash.message != null
        assert ExecutionLog.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/executionLog/list'

        populateValidParams(params)
        def executionLog = new ExecutionLog(params)

        assert executionLog.save() != null

        params.id = executionLog.id

        def model = controller.show()

        assert model.executionLogInstance == executionLog
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/executionLog/list'

        populateValidParams(params)
        def executionLog = new ExecutionLog(params)

        assert executionLog.save() != null

        params.id = executionLog.id

        def model = controller.edit()

        assert model.executionLogInstance == executionLog
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/executionLog/list'

        response.reset()

        populateValidParams(params)
        def executionLog = new ExecutionLog(params)

        assert executionLog.save() != null

        // test invalid parameters in update
        params.id = executionLog.id
        //TODO: add invalid values to params object

        controller.update()

        assert view == "/executionLog/edit"
        assert model.executionLogInstance != null

        executionLog.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/executionLog/show/$executionLog.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        executionLog.clearErrors()

        populateValidParams(params)
        params.id = executionLog.id
        params.version = -1
        controller.update()

        assert view == "/executionLog/edit"
        assert model.executionLogInstance != null
        assert model.executionLogInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/executionLog/list'

        response.reset()

        populateValidParams(params)
        def executionLog = new ExecutionLog(params)

        assert executionLog.save() != null
        assert ExecutionLog.count() == 1

        params.id = executionLog.id

        controller.delete()

        assert ExecutionLog.count() == 0
        assert ExecutionLog.get(executionLog.id) == null
        assert response.redirectedUrl == '/executionLog/list'
    }
}
