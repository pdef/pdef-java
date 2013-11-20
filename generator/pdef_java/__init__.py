# encoding: utf-8
import io
import logging
import os.path

import pdefc
from pdefc.lang import TypeEnum
from pdefc.generators import Namespace, Generator, Templates, upper_first, mkdir_p


UTF8 = 'utf8'
GENERATED_BY = u'Generated by Pdef compiler %s. DO NOT EDIT.' % pdefc.__version__
ENUM_TEMPLATE = 'enum.jinja2'
MESSAGE_TEMPLATE = 'message.jinja2'
INTERFACE_TEMPLATE = 'interface.jinja2'


class JavaGenerator(Generator):
    def __init__(self, out, namespace=None, **kwargs):
        self.out = out
        self.filters = JavaFilters(Namespace(namespace))
        self.templates = Templates(__file__, filters=self.filters)

    def generate(self, package):
        for module in package.modules:
            for def0 in module.definitions:
                self._write(def0)

    def _write(self, def0):
        # Render the code.
        code = self._render(def0)

        # Create module directories.
        package_path = self._package_path(def0)
        mkdir_p(package_path)

        # Write a file.
        path = self._file_path(def0)
        with io.open(path, 'wt', encoding=UTF8) as f:
            f.write(code)

        logging.debug('Created %s', path)

    def _render(self, def0):
        if def0.is_enum:
            return self.templates.render(ENUM_TEMPLATE, enum=def0, generated_by=GENERATED_BY)
        elif def0.is_message:
            return self.templates.render(MESSAGE_TEMPLATE, message=def0, generated_by=GENERATED_BY)
        elif def0.is_interface:
            return self.templates.render(INTERFACE_TEMPLATE, interface=def0,
                                         generated_by=GENERATED_BY)
        raise ValueError('Unsupported definition %r' % def0)

    def _package_path(self, def0):
        package = self.filters.jpackage(def0.module)
        dirs = package.split('.')
        return os.path.join(self.out, os.path.join(*dirs))

    def _file_path(self, def0):
        dirname = self._package_path(def0)
        filename = '%s.java' % def0.name
        return os.path.join(dirname, filename)


class JavaFilters(object):
    '''Java filters for Jinja templates.'''
    def __init__(self, namespace):
        self.namespace = namespace

    def jpackage(self, module):
        return self.namespace(module.name)

    def jdescriptor(self, type0):
        return self.jref(type0).descriptor

    def jdefault(self, type0):
        return self.jref(type0).default

    def jbool(self, expr):
        return 'true' if expr else 'false'

    def jmessage_base(self, message):
        if message.base:
            return self.jref(message.base)

        return 'io.pdef.AbstractException' if message.is_exception else 'io.pdef.AbstractMessage'

    def jfield_get(self, field):
        return 'get%s' % upper_first(field.name)

    def jfield_set(self, field):
        return 'set%s' % upper_first(field.name)

    def jfield_has(self, field):
        return 'has%s' % upper_first(field.name)

    def jfield_clear(self, field):
        return 'clear%s' % upper_first(field.name)

    def jref(self, type0):
        if type0.is_native:
            ref = JAVA_NATIVE_REFS[type0.type]
        else:
            switch = {
                TypeEnum.LIST:  self._jlist,
                TypeEnum.SET:   self._jset,
                TypeEnum.MAP:   self._jmap,
                TypeEnum.ENUM_VALUE: self._jenum_value
            }

            factory = switch.get(type0.type, self._jdefinition)
            ref = factory(type0)

        ref.is_primitive = type0.is_primitive
        ref.is_collection = type0.is_collection
        ref.is_message = type0.is_message
        ref.is_interface = type0.is_interface
        return ref

    def jref_unboxed(self, type0):
        return self.jref(type0).unboxed

    def _jlist(self, type0):
        element = self.jref(type0.element)

        name = 'java.util.List<%s>' % element
        default = 'new java.util.ArrayList<%s>()' % element
        descriptor = 'io.pdef.descriptors.Descriptors.list(%s)' % element.descriptor

        return JavaRef(name, descriptor, default=default)

    def _jset(self, type0):
        element = self.jref(type0.element)

        name = 'java.util.Set<%s>' % element
        default = 'new java.util.HashSet<%s>()' % element
        descriptor = 'io.pdef.descriptors.Descriptors.set(%s)' % element.descriptor

        return JavaRef(name, descriptor, default=default)

    def _jmap(self, type0):
        key = self.jref(type0.key)
        value = self.jref(type0.value)

        name = 'java.util.Map<%s, %s>' % (key, value)
        default = 'new java.util.HashMap<%s, %s>()' % (key, value)
        descr = 'io.pdef.descriptors.Descriptors.map(%s, %s)' % (key.descriptor, value.descriptor)

        return JavaRef(name, descr, default=default)

    def _jenum_value(self, type0):
        name = '%s.%s' % (self.jref(type0.enum), type0.name)
        return JavaRef(name, None)

    def _jdefinition(self, type0):
        package = self.jpackage(type0.module)
        name = '%s.%s' % (package, type0.name)
        descriptor = '%s.DESCRIPTOR' % name
        default = ('new %s()' % name) if type0.is_message else 'null'
        return JavaRef(name, descriptor, default=default)


class JavaRef(object):
    def __init__(self, name, descriptor, default='null', unboxed=None):
        self.name = name
        self.descriptor = descriptor
        self.default = default
        self.unboxed = unboxed or name

        self.is_primitive = True
        self.is_message = False
        self.is_interface = False
        self.is_collection = False

    def __str__(self):
        return self.name


JAVA_NATIVE_REFS = {
    TypeEnum.BOOL:  JavaRef('Boolean',  'io.pdef.descriptors.Descriptors.bool', 'false', 'boolean'),
    TypeEnum.INT16: JavaRef('Short',    'io.pdef.descriptors.Descriptors.int16', '(short) 0', 'short'),
    TypeEnum.INT32: JavaRef('Integer',  'io.pdef.descriptors.Descriptors.int32', '0', 'int'),
    TypeEnum.INT64: JavaRef('Long',     'io.pdef.descriptors.Descriptors.int64', '0L', 'long'),
    TypeEnum.FLOAT: JavaRef('Float',    'io.pdef.descriptors.Descriptors.float0', '0f', 'float'),
    TypeEnum.DOUBLE: JavaRef('Double',  'io.pdef.descriptors.Descriptors.double0', '0.0', 'double'),
    TypeEnum.STRING: JavaRef('String',  'io.pdef.descriptors.Descriptors.string', '""'),
    TypeEnum.VOID: JavaRef('Void',      'io.pdef.descriptors.Descriptors.void0', 'null', 'void'),
    TypeEnum.DATETIME: JavaRef('java.util.Date','io.pdef.descriptors.Descriptors.datetime',
                               'new java.util.Date(0)')
}
