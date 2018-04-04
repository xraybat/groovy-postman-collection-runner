import groovy.json.JsonSlurper

class Resource {
    private _json

    public Resource(def name) {
        _json = new JsonSlurper().parseText(this.getClass().getResource(name).text)
    }
    public getJson() { _json }

} // Resource
