function csrfHeaders(headers = {}) {
    const token = document.querySelector('meta[name="_csrf"]');
    const header = document.querySelector('meta[name="_csrf_header"]');
    return { ...headers, [header.content]: token.content };
}
