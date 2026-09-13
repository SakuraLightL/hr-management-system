const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');

class Element {
    constructor(tag) { this.tag = tag; this.children = []; this.dataset = {}; this.listeners = {}; }
    set innerHTML(value) { throw new Error('API data must never use innerHTML'); }
    appendChild(child) { this.children.push(child); return child; }
    replaceChildren() { this.children = []; }
    createTHead() { return this.appendChild(new Element('thead')); }
    createTBody() { return this.appendChild(new Element('tbody')); }
    insertRow() { return this.appendChild(new Element('tr')); }
    insertCell() { return this.appendChild(new Element('td')); }
    addEventListener(event, callback) { this.listeners[event] = callback; }
}
function load(canEdit) {
    const table = new Element('table'); table.dataset.canEdit = String(canEdit);
    const context = vm.createContext({
        document: {
            addEventListener() {},
            getElementById: id => id === 'employeeTable' ? table : null,
            createElement: tag => new Element(tag),
        },
    });
    vm.runInContext(fs.readFileSync(path.join(__dirname, '../../main/resources/static/js/employees.js'), 'utf8'), context);
    return { context, table };
}
test('employee values, including HTML and script payloads, remain literal text', () => {
    const { context, table } = load(true);
    const payload = '<img src=x onerror=alert(1)>';
    context.renderEmployees([{ id: 3, name: payload, email: payload, position: payload, departmentName: payload }]);
    const row = table.children[1].children[0];
    for (let i = 1; i <= 4; i++) {
        assert.equal(row.children[i].textContent, payload);
        assert.equal(row.children[i].children.length, 0);
    }
    assert.equal(row.children[9].children.length, 2);
});
test('read-only users see no edit or delete controls', () => {
    const { context, table } = load(false);
    context.renderEmployees([{ id: 1, name: 'Alice' }]);
    assert.equal(table.children[0].children[0].children.length, 9);
    assert.equal(table.children[1].children[0].children.length, 9);
});
test('CSRF headers preserve content type and use the server-supplied token header', () => {
    const context = vm.createContext({ document: { querySelector: selector => ({ content: selector.includes('_csrf_header') ? 'X-CSRF-TOKEN' : 'masked-token' }) } });
    vm.runInContext(fs.readFileSync(path.join(__dirname, '../../main/resources/static/js/csrf.js'), 'utf8'), context);
    const headers = context.csrfHeaders({ 'Content-Type': 'application/json' });
    assert.equal(headers['Content-Type'], 'application/json');
    assert.equal(headers['X-CSRF-TOKEN'], 'masked-token');
});
